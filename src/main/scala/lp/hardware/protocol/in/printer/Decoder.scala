package lp.hardware.protocol.in.printer

import lp.model.Base64
import scodec.Attempt.{Failure, Successful}
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs._

object Decoder {

  // little endian, unsigned short(2), unsigned int(4), unsigned int(4)
  private val PayloadHeaderCodec = uint16L ~ uint32L ~ uint32L
  private val PayloadHeaderLength = 10

  private val PowerShortCodec =
    uint32L ~ fixedSizeBytes(24, ascii) ~ fixedSizeBytes(
      24,
      ascii
    ) ~ uint16L ~ uint32L

  private val PowerLongCodec =
    uint32L ~ fixedSizeBytes(32, ascii) ~ fixedSizeBytes(
      32,
      ascii
    ) ~ uint16L ~ uint32L

  private val BC_EVENT_PRODUCT_ANNOUNCE_Length = 20

  // "<I" = Little Endian Unsigned Int
  private val HeartbeatCodec = uint32L

  private val ProductAnnounceCodec =
    ulongL(32) ~ ulongL(32) ~ ulongL(32) ~ ulongL(32) ~ ulongL(
      32
    )

  private val EventDidPrintCodec = byte ~ uint32L

  // TODO find test cases and clean up this logic
  def fromBase64(payload: Base64): Either[String, Event] = {
    val binary = java.util.Base64.getDecoder.decode(payload.value)

    PayloadHeaderCodec.decode(
      BitVector(binary.take(PayloadHeaderLength))
    ) match {
      case Successful(bin) =>
        val ((code, _), payloadLength) = bin.value
        val expectedLength =
          DeviceEventConstants.EVENT_HEADER_SIZE + payloadLength
        if (binary.length != expectedLength)
          Left(
            s"Malformed event `$payload`. Is: ${binary.length} should be: $expectedLength"
          )
        else
          code match {
            case a
                if (a & Constants.BC_EVENT_FORMAT_MASK) == Constants.BC_EVENT_START_BINARY =>
              val eventId = code & Constants.BC_EVENT_ID_MASK
              Right(BergCloudStartBinary(eventId, binary))
            case b
                if (b & Constants.BC_EVENT_FORMAT_MASK) == Constants.BC_EVENT_START_PACKED =>
              val eventId = code & Constants.BC_EVENT_ID_MASK
              Right(BergCloudStartPacked(eventId, binary))
            case c if c == DeviceEventConstants.EVENT_DID_POWER_ON =>
              payloadLength match {
                case DeviceEventConstants.EVENT_DID_POWER_ON_SIZE_LONG =>
                  devicePoweredOn(binary, PowerLongCodec)
                case DeviceEventConstants.EVENT_DID_POWER_ON_SIZE_SHORT =>
                  devicePoweredOn(binary, PowerShortCodec)
                case _ =>
                  Left(s"Malformed event `$payload}`. Is: $payloadLength.")
              }
            case d if d == Constants.BC_EVENT_PRODUCT_ANNOUNCE =>
              payloadLength match {
                case BC_EVENT_PRODUCT_ANNOUNCE_Length =>
                  ProductAnnounceCodec.decode(
                    BitVector(binary.drop(PayloadHeaderLength))
                  ) match {
                    case Successful(announceBin) =>
                      val ((((id0, id1), id2), id3), version) =
                        announceBin.value
                      val productId =
                        "%08d%08d%08d%08d".format(id0, id1, id2, id3)
                      Right(
                        BergCloudProductAnnounce(
                          productId,
                          version
                        )
                      )
                    case Failure(cause) =>
                      Left(
                        s"Malformed event `$payload}`. Cause: ${cause.message}"
                      )
                  }
                case _ =>
                  Left(
                    s"Malformed event `$payload}`. Is: $payloadLength. Expected: 20"
                  )
              }
            case e if e == DeviceEventConstants.EVENT_HEARTBEAT =>
              payloadLength match {
                case DeviceEventConstants.EVENT_HEARTBEAT_SIZE =>
                  Codec
                    .decode(BitVector(binary.drop(PayloadHeaderLength)))(HeartbeatCodec) match {
                    case Successful(heartbeatBin) =>
                      val uptime = heartbeatBin.value
                      Right(DeviceHeartbeat(uptime))
                    case Failure(cause) =>
                      Left(
                        s"Malformed event `$payload}`. Cause: ${cause.message}"
                      )
                  }
                case _ =>
                  Left(
                    s"Malformed event `$payload}`. Is: $payloadLength. Expected: ${DeviceEventConstants.EVENT_HEARTBEAT_SIZE}"
                  )
              }
            case f if f == DeviceEventConstants.EVENT_DID_PRINT =>
              payloadLength match {
                case DeviceEventConstants.EVENT_DID_PRINT_SIZE =>
                  EventDidPrintCodec.decode(BitVector(binary.drop(PayloadHeaderLength))) match {
                    case Successful(printedBin) =>
                      val (printType, printId) = printedBin.value
                      Right(DeviceDidPrint(printType, printId))
                    case Failure(cause) =>
                      Left(
                        s"Malformed event `$payload}`. Cause: ${cause.message}"
                      )
                  }
                case _ =>
                  Left(
                    s"Malformed event `$payload}`. Is: $payloadLength. Expected: ${DeviceEventConstants.EVENT_DID_PRINT_SIZE}"
                  )
              }
            case _ =>
              Left(s" UnknownEvent `$payload}`")
          }
      case Failure(cause) =>
        Left(s"Malformed event `$payload}`. Cause: ${cause.message}")
    }
  }

  private def devicePoweredOn(
      binary: Array[Byte],
      codec: Codec[Long ~ String ~ String ~ Int ~ Long]
  ): Either[String, Event] = {
    codec.decode(BitVector(binary.drop(10))) match {
      case Successful(r) =>
        val (
          (
            ((deviceType, firmwareBuildVersion), loaderBuildVersion),
            protocolVersion
          ),
          resetDescription
        ) = r.value

        DeviceEventConstants.RESET_DICT.get(
          (resetDescription & 0xff00).toInt
        ) match {
          case None =>
            Left(
              s"Malformed event. Invalid reset description. $resetDescription"
            )
          case _ =>
            Right(
              DeviceDidPowerOn(
                deviceType,
                firmwareBuildVersion,
                loaderBuildVersion,
                protocolVersion,
                resetDescription
              )
            )
        }
      case Failure(cause) =>
        Left(s"Malformed event. Cause: ${cause.message}")
    }
  }
}
