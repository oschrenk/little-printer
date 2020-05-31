package lp.hardware.protocol.out.printer

import javax.imageio.ImageIO
import lp.image.{Monochrome, Transformations}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CommandSpec extends AnyFlatSpec with Matchers {

  "Command" should "encode SetDeliveryAndPrint " in {
    val image = new Monochrome.Image(
      Transformations
        .rotate180(ImageIO.read(getClass.getResourceAsStream("/hello.png")))
    )
    val base64 = Command.encode(SetDeliveryAndPrint(1, image)).toOption.get
    base64.value shouldBe "AQABAAEAAAAAAAAAcwAAAG8AAAAAABUAAAAdcwPoHWHQHS8PHUSAGyqAAQAAADABTwAAAPwA+wBxARsHAQIBAQIB+wBMAwEDAQQCARsBAQECBwEC+wBMAQEKAgEbBwECAQT7AE0CAQIBCB4BAQIEAQIB+wBQBAMBAgEhAfsAWQH8ABw="
  }
}
