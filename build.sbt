import Dependencies._

lazy val Name = "littleprinter"
lazy val MainClass = "lp.Main"

lazy val commonSettings = Seq(
  scalaVersion := "2.13.2",
  version := "0.1.0-SNAPSHOT",
  organization := "dev.oschrenk",
  organizationName := "oschrenk",
  // for play-json-refined
  resolvers += Resolver.bintrayRepo("cbley", "maven"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature")
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := Name,
    libraryDependencies ++= Seq(
      akkaHttp,
      akkaStream,
      playJson,
      scodecCore,
      scodecBits,
      refined,
      playJsonRefined,
      pureConfigRefined,
      pureConfig,
      pureConfigGeneric,
      scalaLogging,
      logback,
      scalaTest % Test
    )
  )
