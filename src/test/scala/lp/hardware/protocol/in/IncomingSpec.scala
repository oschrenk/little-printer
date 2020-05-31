package lp.hardware.protocol.in

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class IncomingSpec extends AnyFlatSpec with Matchers {

  private def load(res: String): String = Source.fromResource(res).mkString
  private def parse(res: String): Incoming =
    Incoming.parse(load(res)).toOption.get

  "Parser" should "parse BridgeEvent, power_on " in {
    parse("bridge/power_on.json") shouldBe a[BridgeEvent]
  }

  it should "parse BridgeEvent, device_connect" in {
    parse("bridge/device_connect.json") shouldBe a[BridgeEvent]
  }

  it should "parse BridgeEvent, device_disconnect" in {
    parse("bridge/device_disconnect.json") shouldBe a[BridgeEvent]
  }

  it should "parse BridgeEvent, encryption_key_required" in {
    parse("bridge/encryption_key_required.json") shouldBe a[BridgeEvent]
  }

  it should "parse BridgeLog messages" in {
    parse("bridge/error1.json") shouldBe a[BridgeLog]
    parse("bridge/error2.json") shouldBe a[BridgeLog]
    parse("bridge/error3.json") shouldBe a[BridgeLog]
  }

  it should "parse DeviceCommandResponse" in {
    parse("printer/device_command_response.json") shouldBe a[DeviceCommandResponse]
  }


}
