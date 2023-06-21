import Dependencies.*

lazy val scala213 = "2.13.10"
lazy val scala322 = "3.3.0"
lazy val supportedScalaVersions = List(scala213, scala322)

ThisBuild / organization := "org.dka.book.anorm"
ThisBuild / version := "0.5.1-SNAPSHOT"
ThisBuild / scalaVersion := scala322

lazy val anorm = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "bookAnorm",
    libraryDependencies ++= anormDependencies,
    Defaults.itSettings
  )
