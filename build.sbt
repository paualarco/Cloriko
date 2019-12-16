import Dependencies._

enablePlugins(JavaAppPackaging, DockerPlugin)

mainClass in Compile := Some("com.cloriko.QuickstartServer")

PB.protocVersion := "-v3.10.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.4",
      version         := Version.version
    )),
    name := "Cloriko",
    libraryDependencies ++= ProjectDependencies
  )

PB.targets in Compile := Seq(
  // compile your proto files into scala source files
  scalapb.gen() -> (sourceManaged in Compile).value,
  // generate the GRPCMonix source code
  grpcmonix.generators.GrpcMonixGenerator() -> (sourceManaged in Compile).value
)
scalacOptions += "-Ylog-classpath"
resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
scalacOptions ++= Seq("-Ypartial-unification")



