import sbt._

object Dependencies {

  object DependencyVersions {
    val PureConfig = "0.10.1"
    val Monix = "2.3.3"
    val Circe = "0.11.1"
    val Http4s = "0.20.10"
    val TypesafeConfig = "1.3.2"

    val Log4jScala = "11.0"
    val Log4j = "2.10.0"

    val Scalatest = "3.0.4"
    val Scalacheck = "1.13.5"
    val Mockito = "2.18.3"
  }

  private val MainDependencies = Seq(
    "io.grpc"                   % "grpc-netty"                      % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb"      %% "scalapb-runtime-grpc"           % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb"      %% "scalapb-runtime"                % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "beyondthelines"            %% "grpcmonixgenerator"             % "0.0.7",
    "com.thesamet.scalapb"      %% "compilerplugin"                 % "0.7.0",
    "beyondthelines"            %% "grpcmonixruntime"               % "0.0.7",
    "io.monix"                  %% "monix"                          % DependencyVersions.Monix,
    "com.github.pureconfig"     %% "pureconfig"                     % DependencyVersions.PureConfig,
    "com.typesafe"              % "config"                          % DependencyVersions.TypesafeConfig,
    "org.apache.logging.log4j"  %% "log4j-api-scala"                % DependencyVersions.Log4jScala,
    "org.apache.logging.log4j"  % "log4j-api"                       % DependencyVersions.Log4j,
    "org.apache.logging.log4j"  % "log4j-core"                      % DependencyVersions.Log4j,
    "org.apache.logging.log4j"  % "log4j-slf4j-impl"                % DependencyVersions.Log4j,
    "io.circe"                  %% "circe-core"                     % DependencyVersions.Circe,
    "io.circe"                  %% "circe-generic"                  % DependencyVersions.Circe,
    "io.circe"                  %% "circe-parser"                   % DependencyVersions.Circe,
    "org.http4s"                %% "http4s-server"                  % DependencyVersions.Http4s,
    "org.http4s"                %% "http4s-core"                    % DependencyVersions.Http4s,
    "org.http4s"                %% "http4s-dsl"                     % DependencyVersions.Http4s,
    "org.http4s"                %% "http4s-circe"                   % DependencyVersions.Http4s,
    "org.http4s"                %% "http4s-blaze-server"            % DependencyVersions.Http4s,
    "org.scalacheck"            %% "scalacheck"                     % DependencyVersions.Scalacheck
  )

  private val TestDependencies = Seq(
    "org.scalatest"             %% "scalatest"                      % DependencyVersions.Scalatest,
    "org.scalacheck"            %% "scalacheck"                     % DependencyVersions.Scalacheck,
    "org.mockito"               %  "mockito-core"                   % DependencyVersions.Mockito,
  ).map( _ % Test)

  val ProjectDependencies: Seq[ModuleID] = MainDependencies ++ TestDependencies
}
