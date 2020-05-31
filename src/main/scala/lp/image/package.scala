package lp

import java.awt.image.BufferedImage

package object image {

  implicit class PixelsFromImage(val image: BufferedImage) {
    def toPixels: Seq[Pixel] = {
      for {
        y <- 0 until image.getHeight
        x <- 0 until image.getWidth
      } yield Pixel.apply(x, y, image.getRGB(x, y))
    }
  }

  implicit class ImageFromPixels(val pixels: Seq[Pixel]) {
    def toImage: BufferedImage = {
      val width = pixels.last.x + 1
      val height = pixels.last.y + 1
      val image =
        new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
      pixels.foreach {
        case p @ Pixel(x, y, _, _, _) => image.setRGB(x, y, p.color)
      }
      image
    }
  }

  implicit class MonochromeToDots(val image: Monochrome.Image) {
    def toDots: Seq[Dot] = {
      for {
        y <- 0 until image.value.getHeight
        x <- 0 until image.value.getWidth
        color = image.value.getRGB(x, y)
        dot = if (color == -16777216) Dot.Black else Dot.White
      } yield dot
    }
  }
}
