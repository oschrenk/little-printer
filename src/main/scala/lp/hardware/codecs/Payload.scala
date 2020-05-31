package lp.hardware.codecs

import scodec.Encoder
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * Payload does not care what it's binary content is
  *
  * Payload
  * length | uint32L " length of payload in bytes
  * delimiter | byte " null byte as delimiter
  * data | bytes
  */
case class Payload(size: Long, delimiter: Byte, data: ByteVector)

object Payload {
  implicit val payload: Encoder[Payload] = {
    ("size" | uint32L) ::
      ("delimiter" | byte) ::
      ("data" | bytes)
  }.as[Payload]

  private val DefaultDelimiter: Byte = 0

  def build(headerRegion: ByteVector, data: ByteVector): Payload = {
    Payload(
      headerRegion.length + data.length + 1,
      DefaultDelimiter,
      headerRegion ++ data
    )
  }
}
