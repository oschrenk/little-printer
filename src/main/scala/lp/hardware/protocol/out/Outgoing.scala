package lp.hardware.protocol.out

import lp.model.{Base64, Bridge, Printer}
import play.api.libs.json.{JsObject, Json}

sealed trait Outgoing

case class BridgeCommand(
    bridgeAddress: Bridge.Address,
    commandId: Int,
    timestamp: Float,
    jsonPayload: JsObject
) extends Outgoing

case class DeviceCommand(
    bridgeAddress: Bridge.Address,
    deviceAddress: Printer.Address,
    commandId: Int,
    timestamp: Float,
    binaryPayload: Base64
) extends Outgoing

object Outgoing {
  def encode(o: Outgoing): JsObject = {
    o match {
      case BridgeCommand(a, c, t, j) =>
        Json.obj(
          "type" -> "BridgeCommand",
          "bridge_address" -> s"${a.value}",
          "command_id" -> c,
          "timestamp" -> t,
          "json_payload" -> j
        )
      case DeviceCommand(b, a, c, t, p) =>
        Json.obj(
          "type" -> "DeviceCommand",
          "bridge_address" -> s"${b.value}",
          "device_address" -> s"${a.value}",
          "command_id" -> c,
          "timestamp" -> t,
          "binary_payload" -> p.value
        )
    }
  }
}
