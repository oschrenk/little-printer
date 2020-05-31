import sbt._

object Dependencies {
  lazy val akkaHttpVersion = "10.1.12"
  lazy val akkaStreamVersion = "2.6.5"
  lazy val scalaTestVersion = "3.1.2"

  lazy val playJsonVersion = "2.9.0"
  lazy val scodecCoreVersion = "1.11.7"
  lazy val scodecBitsVersion = "1.1.14"
  lazy val refinedVersion = "0.9.14"
  lazy val playJsonRefinedVersion = "0.8.0"
  lazy val pureConfigVersion = "0.12.3"

  lazy val scalaLoggingVersion = "3.9.2"
  lazy val logbackVersion = "1.2.3"

  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion
  lazy val playJson = "com.typesafe.play" %% "play-json" % playJsonVersion
  lazy val scodecCore = "org.scodec" %% "scodec-core" % scodecCoreVersion
  lazy val scodecBits = "org.scodec" %% "scodec-bits" % scodecBitsVersion
  lazy val refined = "eu.timepit" %% "refined" % refinedVersion
  lazy val playJsonRefined =
    "de.cbley" %% "play-json-refined" % playJsonRefinedVersion
  lazy val pureConfigRefined =
    "eu.timepit" %% "refined-pureconfig" % refinedVersion
  lazy val pureConfig =
    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
  lazy val pureConfigGeneric =
    "com.github.pureconfig" %% "pureconfig-generic" % pureConfigVersion

  lazy val scalaLogging =
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  lazy val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
}
