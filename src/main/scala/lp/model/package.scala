package lp

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Even
import eu.timepit.refined.refineV
import eu.timepit.refined.string.{HexStringSpec, MatchesRegex}
import shapeless.{Witness => W}

package object model {

  type Base64Rules = MatchesRegex[W.`"[a-zA-Z0-9+/=]+"`.T]
  type Base64 = String Refined Base64Rules
  object Base64 {
    def apply(v: String): Either[String, Base64] = {
      refineV[Base64Rules](v)
    }
  }

  // Letter 'A' is omitted and not valid in a claim code.
  type ClaimRules = MatchesRegex[W.`"[0-9B-Z]{16}"`.T]
  type Claim = String Refined ClaimRules
  object Claim {
    def apply(v: String): Either[String, Claim] = {
      refineV[ClaimRules](v.toUpperCase)
    }
  }

  type HexRules = HexStringSpec And Size[Even]
  type Hex = String Refined HexRules
  object Hex {
    def apply(address: String): Either[String, Hex] = {
      refineV[HexRules](address)
    }
  }

}
