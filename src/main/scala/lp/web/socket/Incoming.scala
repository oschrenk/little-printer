package lp.web.socket

import lp.model.{Bridge, Claim, Printer}

object Incoming {
  case class ClaimPrinter(bridge: Bridge.Address, printer: Printer.Address, claim: Claim)
}
