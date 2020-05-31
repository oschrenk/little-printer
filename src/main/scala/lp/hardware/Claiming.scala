package lp.hardware

import java.util.Base64

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import lp.model.Claim

import scala.util.Try

object Claiming {

  /**
    * This dictionary maps base32 digits to five-bit values.
    * Letter 'A' is omitted and not valid in a claim code.
    * Letter 'I' is mapped to number '1' (i.e. claim code is '1', user enters 'I' by mistake).
    * Letter 'L' is mapped to number '1' (i.e. claim code is '1', user enters 'L' by mistake).
    * Letter 'U' is mapped to letter 'v' (i.e. claim code is 'V', user enters 'U' by mistake).
    */
  private val ClaimCodeBase32Dict: Map[Char, Int] = Map(
    '0' -> 0x00,
    '1' -> 0x01,
    '2' -> 0x02,
    '3' -> 0x03,
    '4' -> 0x04,
    '5' -> 0x05,
    '6' -> 0x06,
    '7' -> 0x07,
    '8' -> 0x08,
    '9' -> 0x09,
    // 'A': omitted
    'B' -> 0x0a,
    'C' -> 0x0b,
    'D' -> 0x0c,
    'E' -> 0x0d,
    'F' -> 0x0e,
    'G' -> 0x0f,
    'H' -> 0x10,
    'I' -> 0x01, // mapped to '1'
    'J' -> 0x11,
    'K' -> 0x12,
    'L' -> 0x01, // mapped to '1'
    'M' -> 0x13,
    'N' -> 0x14,
    'O' -> 0x15,
    'P' -> 0x16,
    'Q' -> 0x17,
    'R' -> 0x18,
    'S' -> 0x19,
    'T' -> 0x1a,
    'U' -> 0x1b, // mapped to 'V'
    'V' -> 0x1b,
    'W' -> 0x1c,
    'X' -> 0x1d,
    'Y' -> 0x1e,
    'Z' -> 0x1f
  )

  /**
    * Codes go through the following process:
    *
    * 1. Cleaning
    * 2. Convert from 5-bit to 8-bit (16 chars down to 10 chars)
    * 3. Checksum Check
    * 4. XOR EUI64 + Base64 security hash + CRC are returned
    *
    */
  def process(claim: Claim): Either[Throwable, (BigInt, EncryptionKey)] = {
    unpack(claim) match {
      case Left(t) => Left(t)
      case Right(unpackedClaim) =>
        val rawAs64Bit = unpackedClaim.raw & (BigInt(2).pow(64) - 1)
        val crcData = rawAs64Bit.pack
        val crc = Crc16.check(crcData.toSeq)

        if (unpackedClaim.crc.toInt == crc) {
          // Pack the 40-bit number as a little endian long long,
          // and then truncate back to 5 bytes
          val packedSecret = unpackedClaim.secret.pack.take(5)

          val linkKey = generateLinkKey(packedSecret)
          val linkKeyBase64 = new String(Base64.getEncoder.encode(linkKey))
          val key = EncryptionKey(linkKeyBase64).toOption.get
          Right((unpackedClaim.device, key))
        } else {
          Left(new IllegalArgumentException("CRC Check failed."))
        }
    }
  }

  private val ClaimEncodeList =
    Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j',
      'k', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z')

  def encode(device: BigInt, secret: BigInt): String = {
    val cleanDevice =
      device & (BigInt(2).pow(24) - 1) // 24 bit hardware address xor
    val cleanSecret = secret & (BigInt(2).pow(40) - 1) // 40 bit secret

    val value = cleanDevice | (cleanSecret << 24)
    val data = value.pack
    val crc = Crc16.check(data.toIndexedSeq)
    val cc = value | BigInt(crc) << 64

    val dashes = List(4, 8, 12)
    val code = (0 to 15)
      .foldLeft(("", cc)) {
        case ((text, shift), i) =>
          val newChar = ClaimEncodeList((shift & 0x1f).toInt)
          val value =
            if (dashes.contains(i)) s"$newChar-"
            else newChar.toString
          (value + text, shift >> 5)
      }
      ._1

    code
  }

  private val ZbeeSecConstBlockSize = 16

  private val ClaimCodeSalt: Array[Byte] = Array(
    0x38.toByte,
    0x96.toByte,
    0x10.toByte,
    0xd9.toByte,
    0xb6.toByte,
    0xb1.toByte,
    0x0d.toByte,
    0x16.toByte,
    0x9e.toByte,
    0xe9.toByte,
    0xbf.toByte,
    0x87.toByte,
    0x95.toByte,
    0x32.toByte,
    0x62.toByte,
    0x5b.toByte
  )

  /**
    * This method is inspired by dissectors/packet-zbee-security.c from Wireshark
    * input should be a binary string of 5 bytes in length
    */
  private def generateLinkKey(input: Array[Byte]) = {
    val paddedInput = pad(input)

    // we encrypt the salt and the input bytes
    val firstSpec = new SecretKeySpec(Array.fill[Byte](16)(0x00.toByte), "AES")
    val h1 = encrypt(firstSpec, ClaimCodeSalt) take 16
    val firstOutput = h1.zipWithIndex map {
      case (_, i) =>
        (h1(i) ^ ClaimCodeSalt(i)).toByte
    }

    val secondSpec = new SecretKeySpec(firstOutput, "AES")
    val h2 = encrypt(secondSpec, paddedInput) take 16
    val secondOutput = h2.zipWithIndex map {
      case (_, i) =>
        (h2(i) ^ paddedInput(i)).toByte
    }

    secondOutput
  }

  private def pad(input: Array[Byte]): Array[Byte] = {
    // padding begins with 128_dec = 1000000_bin
    // then pad with zeroes until we're at 14 bytes
    val padded =
      (input :+ 0x80.toByte) ++
        Array
          .fill[Byte](ZbeeSecConstBlockSize - 2 - input.length - 1)(0x00.toByte)

    // pad with the original length encoded as a 16bit int
    val originalLength = input.length + ClaimCodeSalt.length
    padded :+
      ((originalLength * 8) >> 8 & 0xff).toByte :+
      ((originalLength * 8) >> 0 & 0xff).toByte
  }

  private def encrypt(spec: SecretKeySpec, value: Array[Byte]): Array[Byte] = {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, spec)
    cipher.doFinal(value)
  }

  private[hardware] def unpack(
      claim: Claim
  ): Either[Throwable, UnpackedClaim] = {

    Try {
      claim.value.toList.zipWithIndex.map {
        case (c, i) =>
          ClaimCodeBase32Dict.get(c) match {
            case Some(v) => BigInt(v) * BigInt(32).pow(15 - i)
            case None =>
              throw new IllegalArgumentException(s"$c is not a valid character")
          }
      }.sum
    }.toEither.map(UnpackedClaim.apply)
  }
}
