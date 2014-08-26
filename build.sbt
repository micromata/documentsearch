organization  := "de.micromata.sourcetalktage"

name := "documentsearch"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  // elastic search dependencies
  "org.elasticsearch" % "elasticsearch" % "1.3.2",
  "org.elasticsearch" % "elasticsearch-mapper-attachments" % "2.3.1",
  // common dependencies
  "commons-io" % "commons-io" % "2.4"
)
