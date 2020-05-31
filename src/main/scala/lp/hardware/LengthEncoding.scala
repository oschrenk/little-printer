package lp.hardware

import lp.image.Dot
import lp.image.Dot.{Black, White}
import scodec._
import scodec.codecs._

import scala.annotation.tailrec

case class LengthEncoding(count: Int, data: Seq[Byte])

object LengthEncoding {

  def encode(dots: Seq[Dot]): Either[String, LengthEncoding] = {
    // the decoder assumes that the first run is white, so if the
    // first pixel is black, add a zero run of white
    val prefixedRle = prefixedRunLengthEncoding(dots)

    // encode using custom encoding
    val rleLengths = rle(prefixedRle.map(_._1))

    (for {
      lengthBitVector <- Header.codec.encode(Header.build(rleLengths.length))
      lengthBytes = lengthBitVector.toByteArray
      allBytes = lengthBytes ++ rleLengths

    } yield LengthEncoding(dots.length, allBytes.toIndexedSeq)).toEither.left
      .map { err => err.messageWithContext }
  }

  private case class Header(first: Short, length: Long)
  private object Header {
    def build(length: Int): Header = {
      Header(0x01, length.toLong)
    }

    // package up with length as header
    // first byte is compressed type, always 1
    // output = struct.pack("<BL", 0x01, len(compressed_data)) + compressed_data
    // little endian, unsigned char, unsigned long
    val codec: Encoder[Header] = {
      ("first" | ushort8) :: ("length" | uint32L)
    }.as[Header]
  }

  private[hardware] def prefixedRunLengthEncoding(
      dots: Seq[Dot]
  ): Seq[(Int, Dot)] = {
    val rle = runLengthEncoding(dots)
    rle.head._2 match {
      case Black => (0, White) +: rle
      case White => rle
    }
  }

  @tailrec
  private[hardware] def runLengthEncoding[A](
      elems: Seq[A],
      acc: Seq[(Int, A)] = Nil
  ): Seq[(Int, A)] = {
    if (elems.isEmpty)
      acc
    else {
      val (front, back) = elems.span(_ == elems.head)
      runLengthEncoding(back, acc :+ Tuple2(front.length, elems.head))
    }
  }

  /**
    * Custom coding scheme based on run length encoding runs alternating white
    * and black, starting with white
    *
    * The scheme for the littler printer RLE is:
    * - runs of 0..251 inclusive are stored as a byte
    * - if larger, pull off chunks of 1536, 1152, 768, 384, 251 (encoded as
    * 255, 254, 253, 25, 251) until small enough
    *
    * When a large number is broken into chunks, each chunk needs to be
    * suffixed by a zero so it snaps back to swap it back to the correct
    * colour.
    *
    * @param lengths run length encoding runs, alternating white and black, starting with white
    * @return custom encoded black and white image
    */
  private def rle(lengths: Seq[Int]): Seq[Byte] = {
    val Translate =
      Array((1536, 255), (1152, 254), (768, 253), (384, 252), (251, 251))

    @scala.annotation.tailrec
    def chunkAcc(remainder: Int, codes: Seq[Int] = Seq.empty): Seq[Int] = {
      val (chunk: Int, code: Int) = Translate.filter {
        case (c, _) => remainder > c
      }.head
      val r2 = remainder - chunk
      if (r2 > 251)
        chunkAcc(r2, codes :+ code :+ 0)
      else
        codes :+ code :+ 0 :+ r2
    }

    lengths
      .flatMap {
        case l if l <= 251 => Seq(l)
        case l if l > 251  => chunkAcc(l)
      }
      .map(_.toByte)
  }

}
