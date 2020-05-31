package lp.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.Size
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.refineV

case class Printer(address: Printer.Address)

object Printer {
  type AddressRules = HexRules And Size[Equal[16]]
  type Address = String Refined AddressRules

  object Address {
    def apply(hex: String): Either[String, Address] = {
      refineV[AddressRules](hex)
    }
  }
}
