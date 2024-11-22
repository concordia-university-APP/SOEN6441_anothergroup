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
  "com.google.http-client" % "google-http-client-jackson2" % "1.44.2",

  "com.typesafe.akka" %% "akka-actor" % "2.6.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.21",
  "com.typesafe.akka" %% "akka-stream" % "2.6.21"
)

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "5.11.0" % Test,
  "org.mockito" % "mockito-junit-jupiter" % "5.12.0" % Test
)

jacocoReportSettings := JacocoReportSettings()
  .withThresholds(
    JacocoThresholds(
      method = 100,
      line = 100,
      clazz = 100)
  )

jacocoExcludes := Seq(
  "controllers.javascript.*",
  "views.html.*",
  "router.*",
  "controllers.routes*",
  "controllers.ReverseAssets",
  "controllers.ReverseYoutubeController",
)