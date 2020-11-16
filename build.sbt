import Dependencies._

ThisBuild / scalaVersion     := "2.12.9"
ThisBuild / version          := "0.0.1"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "anygrad",
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, major)) if major >= 13 =>
      Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0")
    case _ =>
      Seq()
  }
}

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.7.0-M7"


resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
 
libraryDependencies ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "1.0",

  // Native libraries are not included by default. add this if you want them (as of 0.7)
  // Native libraries greatly improve performance, but increase jar sizes.
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  "org.scalanlp" %% "breeze-natives" % "1.0",

  // The visualization library is distributed separately as well.
  // It depends on LGPL code
  // "org.scalanlp" %% "breeze-viz" % "0.13.1"
)

// https://mvnrepository.com/artifact/org.apache.spark/spark-mllib
// libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.1"
// libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.1"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}