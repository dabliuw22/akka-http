name := "akka-http"

version := "0.1"

scalaVersion := "2.13.0"

val akkaParent = "com.typesafe.akka"
val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.10"
val scalaLoggingParent = "com.typesafe.scala-logging"
val scalaLoggingVersion = "3.9.2"
val logbackParent = "ch.qos.logback"
val logbackVersion = "1.2.3"
val scalaTestParent = "org.scalatest"
val scalaTestVersion = "3.0.8"

libraryDependencies ++= Seq(
  akkaParent %% "akka-stream" % akkaVersion,
  akkaParent %% "akka-http" % akkaHttpVersion,
  akkaParent %% "akka-http-spray-json" % akkaHttpVersion,
  scalaLoggingParent %% "scala-logging" % scalaLoggingVersion,
  logbackParent % "logback-classic" % logbackVersion,

  scalaTestParent %% "scalatest" % scalaTestVersion % Test,
  akkaParent %% "akka-stream-testkit" % akkaVersion % Test,
  akkaParent %% "akka-http-testkit" % akkaHttpVersion % Test,
  akkaParent %% "akka-testkit" % akkaVersion % Test
)