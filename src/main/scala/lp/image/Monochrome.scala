package lp.image

import java.awt.image.BufferedImage

import scala.util.Random

object Monochrome {

  class Image(val value: BufferedImage) extends AnyVal

  val DefaultThreshold = 127

  def threshold(
      original: BufferedImage,
      threshold: Int = DefaultThreshold
  ): Image = {
    new Image(original.toPixels.map {
      case Pixel(x, y, red, green, blue) =>
        if ((red > threshold) | (green > threshold) | (blue > threshold))
          Pixel.apply(x, y, 0xffffff)
        else
          Pixel.apply(x, y, 0x000000)
    }.toImage)
  }

  def average(
      original: BufferedImage,
      threshold: Int = DefaultThreshold
  ): Image = {
    new Image(original.toPixels.map {
      case Pixel(x, y, red, green, blue) =>
        if ((red + green + blue) / 3 < threshold)
          Pixel.apply(x, y, 0x000000)
        else
          Pixel.apply(x, y, 0xffffff)
    }.toImage)
  }

  def dither(original: BufferedImage, r: Random = Random): Image = {
    val threshold = Array(0.25, 0.26, 0.27, 0.28, 0.29, 0.3, 0.31, 0.32, 0.33,
      0.34, 0.35, 0.36, 0.37, 0.38, 0.39, 0.4, 0.41, 0.42, 0.43, 0.44, 0.45,
      0.46, 0.47, 0.48, 0.49, 0.5, 0.51, 0.52, 0.53, 0.54, 0.55, 0.56, 0.57,
      0.58, 0.59, 0.6, 0.61, 0.62, 0.63, 0.64, 0.65, 0.66, 0.67, 0.68, 0.69)

    new Image(original.toPixels.map {
      case Pixel(x, y, red, green, blue) =>
        val lum = (red * 0.21f + green * 0.71f + blue * 0.07f) / 255
        if (lum <= threshold(r.nextInt(threshold.length))) {
          Pixel.apply(x, y, 0x000000)
        } else {
          Pixel.apply(x, y, 0xffffff)
        }
    }.toImage)
  }
}
