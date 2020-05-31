package lp

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Trimmed
import lp.model.Printer
import scodec.Encoder
import scodec.bits.ByteVector

package object hardware {

  type EncryptionKeyRules = Trimmed
  type EncryptionKey = String Refined EncryptionKeyRules

  object EncryptionKey {
    def apply(address: String): Either[String, EncryptionKey] = {
      refineV[EncryptionKeyRules](address)
    }
  }

  type HardwareXor = Int Refined Positive
  object HardwareXor {
    def apply(v: Int): Either[String, HardwareXor] = {
      refineV[Positive](v)
    }
  }

  /**
    * The "hardware xor" is a 3-byte representation of the device_address as an Int.
   **/
  implicit class HardwareXorFromAddress(val deviceAddress: Printer.Address) {
    def toXor: HardwareXor = {
      // little endian
      val b = ByteVector.fromValidHex(deviceAddress.value).reverse
      val claimAddress = new Array[Int](3)

      claimAddress(0) = ((b(0) ^ b(5)) + 256) % 256
      claimAddress(1) = ((b(1) ^ b(3) ^ b(6)) + 256) % 256
      claimAddress(2) = ((b(2) ^ b(4) ^ b(7)) + 256) % 256

      HardwareXor(
        claimAddress(2) << 16 | claimAddress(1) << 8 | claimAddress(0)
      ).toOption.get
    }
  }

  implicit class UnsignedLongLong(val n: BigInt) extends AnyVal {
    def pack: Array[Byte] = littleEndianUnsignedLongLongs(n)
  }

  private val BitsInByte = 8
  private val UnsignedLongLongByteLength = 8
  private val NullByte = 0x00.toByte

  // see also https://docs.python.org/2/library/struct.html
  private def littleEndianUnsignedLongLongs(raw: BigInt): Array[Byte] = {
    // From: http://www.scala-lang.org/api/2.11.8/index.html#scala.math.BigInt@toByteArray:Array[Byte]
    // The byte array will be in big-endian byte-order: the most significant
    // byte is in the zeroth element. The array will contain the minimum number
    // of bytes required to represent this BigInt, including at least one sign
    // bit.
    //
    // This means that we need to cut of the leading byte if it is not the only
    // byte and the most significant byte if it is zero
    val rawArray = raw.toByteArray
    val unsignedArray =
      if (rawArray.length != 1 && rawArray.length * BitsInByte - raw.bitLength == BitsInByte) {
        rawArray.drop(1)
      } else rawArray
    unsignedArray
      .grouped(UnsignedLongLongByteLength)
      .map { b =>
        b.reverse.padTo(UnsignedLongLongByteLength, NullByte)
      }
      .foldLeft(Array.emptyByteArray) {
        case (l, r) =>
          l ++ r
      }
  }

  implicit class Encodable[A](val a: A)(implicit c: Encoder[A]) {
    def encoded: Either[String, ByteVector] = {
      c.encode(a)
        .toEither
        .left
        .map(err => err.messageWithContext)
        .map(_.bytes)
    }
  }

}
