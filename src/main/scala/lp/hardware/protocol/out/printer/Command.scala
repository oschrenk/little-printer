package lp.hardware.protocol.out.printer

import lp.hardware.codecs.{HeaderRegion, Payload, Command => CommandCodec}
import lp.image.Monochrome
import lp.model.Base64
import scodec.bits.ByteVector

sealed trait Command {
  val `type`: Byte
}
object Command {
  def encode(c: Command): Either[String, Base64] = {
    import lp.hardware._
    c match {
      case SetDeliveryAndPrint(messageId, image) =>
        for {
          rle <- LengthEncoding.encode(image.toDots)
          headerRegion <- HeaderRegion.build(rle.count).encoded
          payload <- Right(Payload.build(headerRegion, ByteVector(rle.data)))
          payloadBytes <- payload.encoded
          bytes <- CommandCodec.build(c.`type`.toShort, payloadBytes, messageId).encoded
          base64 <- Base64(
            java.util.Base64.getEncoder.encodeToString(bytes.toArray)
          )
        } yield base64
    }
  }

}

case class SetDeliveryAndPrint(messageId: Int, image: Monochrome.Image) extends Command {
  val `type` = 0x1
}
