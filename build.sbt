
name := """YTProject"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  // Google API Client Library
  "com.google.api-client" % "google-api-client" % "1.32.1",

  // Google OAuth Client Library (with Jetty for LocalServerReceiver)
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.32.1",

  // Google YouTube Data API Library
  "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0",

  // Jackson for JSON parsing
  "com.google.http-client" % "google-http-client-jackson2" % "1.32.1"
)

