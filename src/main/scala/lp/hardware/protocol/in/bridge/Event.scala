package lp.hardware.protocol.in.bridge

import lp.model.Printer
import play.api.libs.json.JsObject

sealed trait Event
object Event {

  def parse(payload: JsObject): Either[String, Event] = {
    (payload \ "name").as[String] match {
      case "power_on" =>
        payload.validate[PowerOn].asEither.left.map(_.toString())
      case "device_connect" =>
        payload.validate[DeviceConnect].asEither.left.map(_.toString())
      case "device_disconnect" =>
        payload.validate[DeviceDisconnect].asEither.left.map(_.toString())
      case "encryption_key_required" =>
        payload.validate[EncryptionKeyRequired].asEither.left.map(_.toString())
      case _ => Left(s"UnknownEvent ${payload.toString()}")
    }
  }
}

case class PowerOn(
    model: String,
    firmware_version: String,
    ncp_version: String,
    local_ip_address: String,
    mac_address: String,
    uptime: String,
    uboot_environment: String,
    network_info: NetworkInfo
) extends Event

case class DeviceConnect(
    device_address: Printer.Address
) extends Event

case class DeviceDisconnect(
    device_address: Printer.Address
) extends Event

case class EncryptionKeyRequired(
    device_address: Printer.Address
) extends Event

case class NetworkInfo(
    network_status: String,
    power: Byte,
    node_eui64: String,
    pan_id: String,
    node_type: String,
    node_id: String,
    security_level: Byte,
    extended_pan_id: String,
    security_profile: String,
    channel: Byte,
    radio_power_mode: String
)

// this data structure breaks with the normal naming structure
// and does not use snake case
case class Record(
    name: String,
    created: Double,
    process: Int,
    levelno: Int,
    processName: String,
    message: String,
    levelname: String
)
