package lp.image

sealed trait Dot
object Dot {
  case object Black extends Dot
  case object White extends Dot
}
