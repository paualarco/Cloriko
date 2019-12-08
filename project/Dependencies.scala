import sbt._

object Dependencies {

  object DependencyVersions {
    val QuillVersion = "3.4.6"

    val http4s = "0.20.10"
    val AkkaHttp = "10.1.10"
    val Akka = "2.5.23"
    val AkkaLog4j = "1.6.1"
    val AkkaStreamCassandra = "1.1.1"
    val DatastaxDriverExtras = "3.3.0"
    val TypesafeConfig = "1.3.2"

    val Log4jScala = "11.0"
    val Log4j = "2.10.0"

    val Scalatest = "3.0.4"
    val Scalacheck = "1.13.5"
    val Mockito = "2.18.3"
    val PureConfig = "0.10.1"

    val monix = "2.3.3"
    val circeVersion = "0.11.1"
  }

  private val MainDependencies = Seq(
    "io.grpc"                   % "grpc-netty"                      % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb"      %% "scalapb-runtime-grpc"           % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb"      %% "scalapb-runtime"                % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "beyondthelines"            %% "grpcmonixgenerator"             % "0.0.7",
    "com.thesamet.scalapb"      %% "compilerplugin"                 % "0.7.0",
    "beyondthelines"            %% "grpcmonixruntime"               % "0.0.7",
     "io.monix"                 %% "monix"                          % DependencyVersions.monix,
    "com.typesafe.akka"         %% "akka-actor-typed"               % DependencyVersions.Akka,
    "com.github.pureconfig"     %% "pureconfig"                     % DependencyVersions.PureConfig,
    "com.typesafe.akka"         %% "akka-stream"                    % DependencyVersions.Akka,
    "com.typesafe.akka"         %% "akka-actor"                     % DependencyVersions.Akka,
    "com.typesafe.akka"         %% "akka-http-spray-json"           % DependencyVersions.AkkaHttp,
    "de.heikoseeberger"         %% "akka-log4j"                     % DependencyVersions.AkkaLog4j,
    "com.typesafe"              % "config"                          % DependencyVersions.TypesafeConfig,
    "com.lightbend.akka"        %% "akka-stream-alpakka-cassandra"  % DependencyVersions.AkkaStreamCassandra,
    "com.datastax.cassandra"    % "cassandra-driver-extras"         % DependencyVersions.DatastaxDriverExtras,
    "org.apache.logging.log4j"  %% "log4j-api-scala"                % DependencyVersions.Log4jScala,
    "org.apache.logging.log4j"  % "log4j-api"                       % DependencyVersions.Log4j,
    "org.apache.logging.log4j"  % "log4j-core"                      % DependencyVersions.Log4j,
    "org.apache.logging.log4j"  % "log4j-slf4j-impl"                % DependencyVersions.Log4j,
    "com.typesafe.akka"         %% "akka-actor-testkit-typed"       % DependencyVersions.Akka % Test,
    "io.circe"                  %% "circe-core"                     % DependencyVersions.circeVersion,
    "io.circe"                  %% "circe-generic"                  % DependencyVersions.circeVersion,
    "io.circe"                  %% "circe-parser"                   % DependencyVersions.circeVersion,
    "org.http4s"                %% "http4s-server"                  % DependencyVersions.http4s,
    "org.http4s"                %% "http4s-core"                    % DependencyVersions.http4s,
    "org.http4s"                %% "http4s-dsl"                     % DependencyVersions.http4s,
    "org.http4s"                %% "http4s-circe"                   % DependencyVersions.http4s,
    "org.http4s"                %% "http4s-blaze-server"            % DependencyVersions.http4s
  )

  private val TestDependencies = Seq(
    "org.scalatest"             %% "scalatest"                      % DependencyVersions.Scalatest,
    "org.scalacheck"            %% "scalacheck"                     % DependencyVersions.Scalacheck,
    "org.mockito"               %  "mockito-core"                   % DependencyVersions.Mockito,
    "com.typesafe.akka"         %% "akka-testkit"                   % DependencyVersions.Akka,
    "com.typesafe.akka"         %% "akka-stream-testkit"            % DependencyVersions.Akka,
    "com.typesafe.akka"         %% "akka-http-spray-json"           % DependencyVersions.AkkaHttp,
    "com.typesafe.akka"         %% "akka-actor-testkit-typed"       % DependencyVersions.Akka
  ).map( _ % Test)


  val ProjectDependencies: Seq[ModuleID] = MainDependencies ++ TestDependencies
}
