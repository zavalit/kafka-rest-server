enablePlugins(JavaAppPackaging)

name         := "kafka-rest-server"
organization := "io.pedantic"
version      := "0.1"
scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

mainClass in Compile := Some("KafkaRestServer")

libraryDependencies ++= {
  val akkaV       = "2.4.2"
  val akkaStreamV = "2.0.3"
  val scalaTestV  = "2.2.5"
  val kafkaV      = "0.9.0.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV,
    "org.apache.kafka"  %% "kafka"                                % kafkaV,
    "org.scalatest"     %% "scalatest"                            % scalaTestV % "test"
  )
}



Revolver.settings
