addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.4")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.27")
//libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0"
//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)


resolvers += Resolver.bintrayRepo("beyondthelines", "maven")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.7.0",
  "beyondthelines"       %% "grpcmonixgenerator" % "0.0.7"
)