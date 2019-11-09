import Dependencies._

enablePlugins(JavaAppPackaging, DockerPlugin)

mainClass in Compile := Some("com.cloriko.QuickstartServer")

lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion    = "2.5.26"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.8",
      version         := Version.version
    )),
    name := "Cloriko",
    libraryDependencies ++= ProjectDependencies
  )
