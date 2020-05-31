package lp.web.socket

import lp.model.{Bridge, Printer}

sealed trait Outgoing
object Outgoing {
  case class Heartbeat(bridge: Bridge.Address, printer: Printer.Address) extends Outgoing
  case class UnclaimedPrinter(bridge: Bridge.Address, printer: Printer.Address) extends Outgoing
}
