package lp.web

import de.cbley.refined.play.json._
import lp.web.socket.Incoming.ClaimPrinter
import lp.web.socket.Outgoing.{Heartbeat, UnclaimedPrinter}
import play.api.libs.json.{Json, Reads, Writes}

package object socket {

  implicit val outgoingWrites: Writes[Outgoing] = {
    case o: Heartbeat        => Json.toJson(o)
    case o: UnclaimedPrinter => Json.toJson(o)
  }

  // TODO make the outer structure explicit
  implicit val heartbeatWrites: Writes[Heartbeat] = (heartbeat: Heartbeat) =>
    Json.obj(
      "type" -> "heartbeat",
      "payload" -> Json.obj(
        "bridge" -> heartbeat.bridge.value,
        "printer" -> heartbeat.printer.value
      )
    )

  implicit val unclaimedWrites: Writes[UnclaimedPrinter] = (unclaimedPrinter: UnclaimedPrinter) =>
    Json.obj(
      "type" -> "unclaimed_printer",
      "payload" -> Json.obj(
        "bridge" -> unclaimedPrinter.bridge.value,
        "printer" -> unclaimedPrinter.printer.value
      )
    )

  implicit val claimReads: Reads[ClaimPrinter] =
    Json.reads[ClaimPrinter]

}
