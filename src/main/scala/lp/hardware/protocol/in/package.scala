package lp.hardware.protocol

import de.cbley.refined.play.json._
import lp.hardware.protocol.in.bridge.Record
import play.api.libs.json.{Json, Reads}

package object in {

  implicit val bridgeEventReads: Reads[BridgeEvent] =
    Json.reads[BridgeEvent]

  implicit val bridgeCommandResponseReads: Reads[BridgeCommandResponse] =
    Json.reads[BridgeCommandResponse]

  implicit val deviceCommandResponseReads: Reads[DeviceCommandResponse] =
    Json.reads[DeviceCommandResponse]

  implicit val deviceEventReads: Reads[DeviceEvent] =
    Json.reads[DeviceEvent]

  implicit val recordReads: Reads[Record] =
    Json.reads[Record]

  implicit val bridgeLogReads: Reads[BridgeLog] =
    Json.reads[BridgeLog]

}
