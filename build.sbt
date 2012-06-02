//basic project info
name := "natto"

organization := "com.catamorphic"

version := "1.0.0-SNAPSHOT"

//scala versions and options
scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.1")

scalacOptions ++= Seq("-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

//dependencies
libraryDependencies ++= Seq (
  "com.chuusai" %% "shapeless" % "1.2.0",
  "org.scalacheck" %% "scalacheck" % "1.9" % "test",
  "org.specs2" %% "specs2" % "1.10" % "test"
)

//improve REPL
initialCommands := """|import scalaz._, Scalaz._
                      |import shapeless._, Record._, HList._
                      |import natto._
                      |""".stripMargin

//only uncomment if you need dependencies from the snapshots repo
//resolvers += ScalaToolsSnapshots

//sbt behavior
logLevel in compile := Level.Warn

traceLevel := 5

//dependecy-graph-plugin
net.virtualvoid.sbt.graph.Plugin.graphSettings

//start-script-plugin
seq(
  com.typesafe.startscript.StartScriptPlugin.startScriptForClassesSettings: _*
)

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "releases"  at "http://oss.sonatype.org/content/repositories/releases")

//buildinfo-plugin
buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[Scoped](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "com.example"
