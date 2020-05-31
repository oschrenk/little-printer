package lp.hardware

case class UnpackedClaim private (raw: BigInt, device: BigInt, secret: BigInt, crc: BigInt)
object UnpackedClaim {
  def apply(raw: BigInt): UnpackedClaim = {
    val device: BigInt =
      raw & (BigInt(2).pow(24) - 1) // 24 bit hardware address xor
    val secret: BigInt = (raw >> 24) & (BigInt(2).pow(40) - 1) // 40 bit secret
    val crc: BigInt = raw >> 64 // 16 bit crc

    UnpackedClaim(raw, device, secret, crc)
  }
}

