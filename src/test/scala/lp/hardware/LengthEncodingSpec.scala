package lp.hardware

import lp.image.{Dot, Monochrome, Pixel}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LengthEncodingSpec extends AnyFlatSpec with Matchers {

  "Length encoding" should "do run length encoding" in {
    val b = Dot.Black
    val w = Dot.White
    val values = Seq(b, b, b, b, b, w, w, w)

    LengthEncoding
      .runLengthEncoding[Dot](values, Seq.empty) shouldBe Seq((5, b), (3, w))
  }

  it should "do prefix length encoding if it starts with black" in {
    val b = Dot.Black
    val w = Dot.White
    val values = Seq(b, b, b, b, b, w, w, w)

    LengthEncoding
      .prefixedRunLengthEncoding(values) shouldBe Seq((0, w), (5, b), (3, w))
  }

  it should "not do prefix length encoding if it starts with white" in {
    val b = Dot.Black
    val w = Dot.White
    val values = Seq(w, w, w, b, b, b, b, b)

    LengthEncoding
      .prefixedRunLengthEncoding(values) shouldBe Seq((3, w), (5, b))
  }

  it should "do custom encoding" in {
    val pixels = for {
      x <- 0.to(2)
      y <- 0.to(2)
      p = if ((x + y) % 2 == 0) Pixel.white(x, y) else Pixel.black(x, y)
    } yield p
    val dots = Monochrome.threshold(pixels.toImage).toDots
    val ei = LengthEncoding.encode(dots).toOption.get
    ei.count shouldBe 9
    ei.data shouldBe Seq(1, 9, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1)
  }

}
