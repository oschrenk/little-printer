package lp.image

object Pixel {
  def apply(x: Int, y: Int, color: Int): Pixel = {
    val red = (color >>> 16) & 0xff
    val green = (color >>> 8) & 0xff
    val blue = (color >>> 0) & 0xff
    Pixel(x, y, red, green, blue)
  }
  def black(x: Int, y: Int): Pixel = apply(x, y, 0)
  def white(x: Int, y: Int): Pixel = apply(x, y, 255, 255, 255)
}

case class Pixel private (x: Int, y: Int, red: Int, green: Int, blue: Int) {
  val color: Int = (red << 16) + (green << 8) + blue
}
