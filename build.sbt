name := """YTProject"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  // Google API Client Library
  "com.google.api-client" % "google-api-client" % "2.6.0",

  // Google OAuth Client Library (with Jetty for LocalServerReceiver)
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.36.0",

  // Google YouTube Data API Library
  "com.google.apis" % "google-api-services-youtube" % "v3-rev20240514-2.0.0",

  // Jackson for JSON parsing
  "com.google.http-client" % "google-http-client-jackson2" % "1.44.2"
)

libraryDependencies ++= Seq(
  "org.junit.jupiter" % "junit-jupiter-api" % "5.9.2" % Test,
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.9.2" % Test,
  "org.mockito" % "mockito-core" % "5.4.0" % Test,
  "org.mockito" % "mockito-junit-jupiter" % "5.4.0" % Test
)