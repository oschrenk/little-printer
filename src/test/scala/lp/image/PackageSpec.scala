package lp.image

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PackageSpec extends AnyFlatSpec with Matchers {

  "Pixel sequence" should "be converted to image" in {
    val pixels = for {
      y <- 0.to(1)
      x <- 0.to(1)
      p = if ((x + y) % 2 == 0) Pixel.white(x, y) else Pixel.black(x, y)
    } yield p
    pixels.toImage.toPixels shouldBe pixels
  }

  "Monochrome image" should "be converted to dots" in {
    val pixels = for {
      y <- 0.to(1)
      x <- 0.to(1)
      p = if ((x + y) % 2 == 0) Pixel.white(x, y) else Pixel.black(x, y)
    } yield p
    val dots = Monochrome.threshold(pixels.toImage).toDots
    dots shouldBe Seq(Dot.White, Dot.Black, Dot.Black, Dot.White)
  }

}
