package lp.hardware.codecs

import scodec.Encoder
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * header_region
  * delimiter | byte " null byte as delimiter/leading
  * length | len(printer_control) + len(printer_data) + 1 = length of header region in bytes
  * printer_control | 13 prefined bytes, controlling printer
  * printer_data | 8 bytes describing content length
  */
case class HeaderRegion(lead: Byte, size: Long, data: ByteVector)
object HeaderRegion {
  implicit val header: Encoder[HeaderRegion] = {
    ("lead" | byte) ::
      ("size" | uint32L) ::
      ("data" | bytes)
  }.as[HeaderRegion]

  private val PrinterControl = ByteVector(
    0x1d, 0x73, 0x03, 0xe8, // max printer speed
    0x1d, 0x61, 0xd0, // printer acceleration
    0x1d, 0x2f, 0x0f, // peak current
    0x1d, 0x44, 0x80 // max intensity
  )
  private val LeadingByte: Byte = 0

  def build(pixelCount: Int): HeaderRegion = {
    val printerByteCount = pixelCount / 8
    val (n3, n3Remainder) = (printerByteCount / 65536, printerByteCount % 65536)
    val (n2, n1) = (n3Remainder / 256, n3Remainder % 256)

    val printerData = ByteVector(0x1b, 0x2a, n1, n2, n3, 0, 0, 48)

    HeaderRegion(
      LeadingByte,
      PrinterControl.length + printerData.length,
      PrinterControl ++ printerData
    )
  }
}
