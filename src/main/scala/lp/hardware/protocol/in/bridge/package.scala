package lp.hardware.protocol.in

import de.cbley.refined.play.json._
import play.api.libs.json.{Json, Reads}

package object bridge {

  implicit val networkInfoReads: Reads[NetworkInfo] =
    Json.reads[NetworkInfo]

  implicit val power_onReads: Reads[PowerOn] =
    Json.reads[PowerOn]

  implicit val device_connectReads: Reads[DeviceConnect] =
    Json.reads[DeviceConnect]

  implicit val device_disconnectReads: Reads[DeviceDisconnect] =
    Json.reads[DeviceDisconnect]

  implicit val encryption_key_requiredReads: Reads[EncryptionKeyRequired] =
    Json.reads[EncryptionKeyRequired]
}
