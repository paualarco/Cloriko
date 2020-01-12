PB.targets in Compile := Seq(
  // compile your proto files into scala source files
  scalapb.gen() -> (sourceManaged in Compile).value,
  // generate the GRPCMonix source code
  grpcmonix.generators.GrpcMonixGenerator() -> (sourceManaged in Compile).value
)
scalacOptions += "-Ylog-classpath"
resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
scalacOptions ++= Seq("-Ypartial-unification")
