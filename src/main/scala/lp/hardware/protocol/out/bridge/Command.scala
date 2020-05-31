package lp.hardware.protocol.out.bridge

import lp.hardware.EncryptionKey
import lp.model.Printer
import play.api.libs.json.{JsObject, Json}

sealed trait Command
object Command {
  def encode(c: Command): JsObject = {
    c match {
      case AddDeviceEncryptionKey(d, k) =>
        Json.obj(
          "name" -> "add_device_encryption_key",
          "params" -> Json.obj(
            "device_address" -> s"${d.value}",
            "encryption_key" -> s"${k.value}"
          )
        )
    }
  }
}

case class AddDeviceEncryptionKey(
    deviceAddress: Printer.Address,
    key: EncryptionKey
) extends Command
