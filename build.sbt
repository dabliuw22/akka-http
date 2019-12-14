name := "akka-http"

version := "0.1"

scalaVersion := "2.13.0"

val akkaParent = "com.typesafe.akka"
val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.10"
val jwtSprayJsonParent = "com.pauldijou"
val jwtSprayJsonVersion = "4.1.0"
val scalaLoggingParent = "com.typesafe.scala-logging"
val scalaLoggingVersion = "3.9.2"
val logbackParent = "ch.qos.logback"
val logbackVersion = "1.2.3"
val logbackEncoderParent = "net.logstash.logback"
val logbackEncoderVersion = "6.3"
val scalaTestParent = "org.scalatest"
val scalaTestVersion = "3.0.8"
val scalaMockParent = "org.scalamock"
val scalaMockVersion = "4.4.0"

libraryDependencies ++= Seq(
  akkaParent %% "akka-stream" % akkaVersion,
  akkaParent %% "akka-http" % akkaHttpVersion,
  akkaParent %% "akka-http-spray-json" % akkaHttpVersion,
  jwtSprayJsonParent %% "jwt-spray-json" % jwtSprayJsonVersion,
  scalaLoggingParent %% "scala-logging" % scalaLoggingVersion,
  logbackParent % "logback-classic" % logbackVersion,
  logbackEncoderParent % "logstash-logback-encoder" % logbackEncoderVersion,

  scalaTestParent %% "scalatest" % scalaTestVersion % Test,
  scalaMockParent %% "scalamock" % scalaMockVersion % Test,
  akkaParent %% "akka-stream-testkit" % akkaVersion % Test,
  akkaParent %% "akka-http-testkit" % akkaHttpVersion % Test,
  akkaParent %% "akka-testkit" % akkaVersion % Test
)