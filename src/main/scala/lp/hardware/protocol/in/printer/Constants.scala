package lp.hardware.protocol.in.printer

// private constant are not used in the project
// but were mentioned in the original
object Constants {
  val BC_EVENT_PRODUCT_ANNOUNCE = 0xa000
  private val BC_COMMAND_SET_BERGCLOUD_ID = 0xb000

  private val BC_COMMAND_START_BINARY = 0xc000
  private val BC_COMMAND_START_PACKED = 0xc100
  private val BC_COMMAND_ID_MASK = 0x00ff
  private val BC_COMMAND_FORMAT_MASK = 0xff00

  private val BC_COMMAND_DISPLAY_IMAGE = 0xd000
  private val BC_COMMAND_DISPLAY_TEXT = 0xd001

  val BC_EVENT_START_BINARY = 0xe000
  val BC_EVENT_START_PACKED = 0xe100
  val BC_EVENT_ID_MASK = 0x00ff

  val BC_EVENT_FORMAT_MASK = 0xff00

  private val BC_COMMAND_FIRMWARE_ARDUINO = 0xf010
  private val BC_COMMAND_FIRMWARE_MBED = 0xf020
}

object DeviceEventConstants {

  val EVENT_HEADER_SIZE = 10

  val EVENT_HEARTBEAT = 1
  val EVENT_DID_PRINT = 2
  val EVENT_DID_POWER_ON = 3

  val EVENT_HEARTBEAT_SIZE = 4
  val EVENT_DID_PRINT_SIZE = 5
  val EVENT_DID_POWER_ON_SIZE_LONG = 74 // Using fragmentation
  val EVENT_DID_POWER_ON_SIZE_SHORT = 58 // No fragmentation

  val RESET_DICT = Map(
    0x0000 -> "Undeterminable cause",
    0x0100 -> "FIB bootloader",
    0x0200 -> "Ember bootloader",
    0x0300 -> "External reset",
    0x0400 -> "Power on",
    0x0500 -> "Watchdog",
    0x0600 -> "Software triggered",
    0x0700 -> "Software crash",
    0x0800 -> "Flash failure",
    0x0900 -> "Fatal error",
    0x0a00 -> "Access fault"
  )
}
