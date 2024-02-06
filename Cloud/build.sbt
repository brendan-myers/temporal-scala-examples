ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "temporal-cloud-example",

    libraryDependencies ++= Seq(
      "io.temporal" % "temporal-sdk" % "1.22.3",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.3",
      "org.slf4j" % "slf4j-api" % "2.0.5",
      "org.slf4j" % "slf4j-simple" % "2.0.5",
    )
  )
