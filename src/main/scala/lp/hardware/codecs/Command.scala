package lp.hardware.codecs

import scodec.Encoder
import scodec.bits.ByteVector
import scodec.codecs._

object Command {
  // device type c, reserved byte c,
  // command_name <H (short), file_id <L (long),
  // unimplemented CRC (long is zero)
  implicit val command: Encoder[Command] = {
    ("deviceType" | byte) ::
      ("reservedByte" | byte) ::
      ("commandId" | uint16L) ::
      ("printId" | uint32L) ::
      ("unimplementedCRC" | uint32L) ::
      ("payloadLength" | uint32L) ::
      ("payload" | bytes)
  }.as[Command]

  private val LittlePrinterDeviceId: Byte = 1
  private val ReservedBytes: Byte = 0
  private val UnimplementedCRC: Long = 0

  def build(commandType: Short, payload: ByteVector, messageId: Int): Command = {
    Command(
      LittlePrinterDeviceId,
      ReservedBytes,
      commandType.toInt,
      messageId.toLong,
      UnimplementedCRC,
      payload.length,
      payload
    )
  }
}
case class Command(
    deviceType: Byte,
    reservedByte: Byte,
    commandId: Int,
    printId: Long,
    unimplementedCRC: Long,
    payloadLength: Long,
    payload: ByteVector
)
