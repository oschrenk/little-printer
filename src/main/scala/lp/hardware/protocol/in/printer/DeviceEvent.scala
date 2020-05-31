package lp.hardware.protocol.in.printer

sealed trait Event

case class DeviceDidPowerOn(
    deviceType: Long,
    firmwareBuildVersion: String,
    loaderBuildVersion: String,
    protocolVersion: Int,
    resetDescription: Long
) extends Event

case class BergCloudStartBinary(
    eventId: Int,
    data: Array[Byte]
) extends Event

case class BergCloudStartPacked(
    eventId: Int,
    data: Array[Byte]
) extends Event

case class BergCloudProductAnnounce(
    productId: String,
    productVersion: Long
) extends Event

/**
  * Device will send a heartbeat every 10 seconds
  * @param uptime Relative time in seconds since device was connected
  */
case class DeviceHeartbeat(
    uptime: Long
) extends Event

// 0x01 => :delivery,
// 0x10 => :nothing_to_print,
// 0x11 => :quip,
case class DeviceDidPrint(`type`: Byte, id: Long) extends Event
