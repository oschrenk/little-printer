package lp.hardware

import lp.model.Claim
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ClaimingSpec extends AnyFlatSpec with Matchers {

  "Claiming" should "decode" in {
    val claimCode = Claim.apply("6xwh441j8115zyrh").toOption.get
    val expectedEncryptionKey = "F7D9bmztHV32+WJScGZR0g=="

    Claiming
      .process(claimCode)
      .toOption
      .get
      ._2
      .value shouldEqual expectedEncryptionKey
  }
}
