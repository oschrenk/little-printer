package lp.hardware

import lp.model.Printer.Address
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.{Table, forAll}

class PackageSpec extends AnyFlatSpec with Matchers {
  private val table = Table(
    ("address", "xor"),
    ("aaaaaaaaaaaaaaaa", 11184640),
    ("940f1915082e6413", 11494154),
    ("c9a1ae6f1eb93975", 2066139),
    ("78c64b8c2156f37b", 10622000),
    ("8d904803ac6765c3", 15292811)
  )

  "Address" should "convert to hardware Xor" in {
    forAll(table) { (a: String, x: Int) =>
      Address(a).toOption.get.toXor.value shouldBe x
    }
  }

}
