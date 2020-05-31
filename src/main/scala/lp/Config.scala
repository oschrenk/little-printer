package lp

import eu.timepit.refined.pureconfig._
import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.generic.auto._

case class Config(
    port: Int,
    interface: String
)
object Config {
  def load: Result[Config] = ConfigSource.default.load[Config]
}
