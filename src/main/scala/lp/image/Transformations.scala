package lp.image

import java.awt.Image
import java.awt.image.BufferedImage

object Transformations {
  def resizeToWidth(original: BufferedImage, newWidth: Int): BufferedImage = {
    val newHeight =
      ((newWidth.toDouble / original.getWidth()) * original.getHeight()).toInt

    val tmp =
      original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
    val dimg = new BufferedImage(newWidth, newHeight, original.getType)

    val g2d = dimg.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()

    dimg
  }

  def rotate180(original: BufferedImage): BufferedImage = {
    def reindex(pixels: Array[Pixel]): Seq[Pixel] = {
      val reversed = pixels.reverse
      val width = pixels.last.x + 1
      val height = pixels.last.y + 1
      for {
        i <- 0 until width
        j <- 0 until height
      } yield Pixel.apply(i, j, reversed(j * width + i).color)
    }

    reindex(original.toPixels.toArray).toImage
  }

}
