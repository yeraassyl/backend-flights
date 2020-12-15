name := "flights"

version := "0.1"

scalaVersion := "2.13.4"

idePackagePrefix := Some("kz.kbtu.flights")

val akkaVersion = "2.6.10"
val akkaHttpVersion = "10.2.1"
val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka"%% "akka-actor-testkit-typed" % akkaVersion % Test,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",
  "org.scalatest" %% "scalatest" % "3.1.4" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
)