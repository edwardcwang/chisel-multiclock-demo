version := "3.1.0"
scalaVersion := "2.13.10"

val chiselVersion = "3.6.0"

// Depend on AsyncQueue
// ACHTUNG: sbt gets confused if the Scala versions of the parent and
// child project don't match!
lazy val asyncqueue = (project in file("asyncqueue-lite"))
lazy val root = (project in file(".")).dependsOn(asyncqueue)
  .settings(
    name := "chisel-module-template",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.6.0" % "test"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )
