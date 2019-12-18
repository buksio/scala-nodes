name := "nodes"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "org.apache.poi" % "poi" % "4.1.1",
  "org.apache.poi" % "poi-ooxml" % "4.1.1",
  "net.liftweb" %% "lift-json" % "3.4.0",
  "com.typesafe.akka" %% "akka-http"   % "10.1.11",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26"
)
