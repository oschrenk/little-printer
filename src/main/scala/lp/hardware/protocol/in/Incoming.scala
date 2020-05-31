package lp.hardware.protocol.in

import lp.hardware.protocol.in.bridge.Record
import lp.model.{Base64, Bridge, Printer}
import play.api.libs.json.{JsObject, JsValue, Json}

sealed trait Incoming

object Incoming {
  def parse(json: String): Either[String, Incoming] = {
    decodeMessage(Json.parse(json))
  }

  private def decodeMessage(data: JsValue): Either[String, Incoming] = {
    // deal with polymorphic message, containing self describing type
    (data \ "type").as[String] match {
      case "BridgeEvent" =>
        data.validate[BridgeEvent].asEither.left.map(_.toString())
      case "BridgeLog" =>
        data.validate[BridgeLog].asEither.left.map(_.toString())
      case "BridgeCommandResponse" =>
        data.validate[BridgeCommandResponse].asEither.left.map(_.toString())
      case "DeviceCommandResponse" =>
        data.validate[DeviceCommandResponse].asEither.left.map(_.toString())
      case "DeviceEvent" =>
        data.validate[DeviceEvent].asEither.left.map(_.toString())
      case _ =>
        Left(s"Unknown event ${data.toString()}")
    }
  }
}

case class BridgeEvent(
    bridge_address: Bridge.Address,
    timestamp: Float,
    json_payload: JsObject
) extends Incoming

case class BridgeLog(bridge_address: Bridge.Address, records: Seq[Record]) extends Incoming

case class BridgeCommandResponse(
    bridge_address: Bridge.Address,
    command_id: Int,
    return_code: Int,
    timestamp: Float
) extends Incoming

case class DeviceCommandResponse(
    bridge_address: Bridge.Address,
    device_address: Printer.Address,
    timestamp: Float,
    command_id: Int,
    return_code: Int,
    transfer_time: Float,
    rssi_stats: Array[Int]
) extends Incoming

case class DeviceEvent(
    bridge_address: Bridge.Address,
    device_address: Printer.Address,
    timestamp: Float,
    rssi_stats: Array[Int],
    binary_payload: Base64
) extends Incoming
