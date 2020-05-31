package lp.hardware.protocol.in.bridge

import lp.hardware.protocol.in.{BridgeEvent, Incoming}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class EventSpec extends AnyFlatSpec with Matchers {

  private def load(res: String): String = Source.fromResource(res).mkString
  private def parse(res: String): Event = {
    val i = Incoming.parse(load(res)).toOption.get.asInstanceOf[BridgeEvent]
    Event.parse(i.json_payload).toOption.get
  }

  "bridge.Event" should "parse BridgeEvent, power_on " in {
    parse("bridge/power_on.json") shouldBe a[PowerOn]
  }

  it should "parse BridgeEvent, device_connect" in {
    parse("bridge/device_connect.json") shouldBe a[DeviceConnect]
  }

  it should "parse BridgeEvent, device_disconnect" in {
    parse("bridge/device_disconnect.json") shouldBe a[DeviceDisconnect]
  }

  it should "parse BridgeEvent, encryption_key_required" in {
    parse("bridge/encryption_key_required.json") shouldBe a[
      EncryptionKeyRequired
    ]
  }

}
