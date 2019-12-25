package com.cloriko.master

import cats.implicits._
import cats.effect.{ ExitCode, IO, IOApp }
import com.cloriko.config.AppConfig
import com.cloriko.master.grpc.GrpcServer
import org.http4s.implicits._
import org.http4s.server.blaze._
import com.cloriko.master.http.{ OperationalRoutes, UserAuthRoutes }
import org.http4s.server.Router

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object WebServer extends IOApp with OperationalRoutes with UserAuthRoutes {

  val cloriko: Cloriko = new Cloriko

  val config: AppConfig = AppConfig.load()
  val host = config.server.host
  val port = config.server.port

  val endPoint = config.server.endPoint

  println(s"Starting web server on endpoint: $endPoint")

  Future(new GrpcServer(endPoint, cloriko).start().blockUntilShutdown())

  val routes = userRoutes <+> operationalRoutes

  val httpApp = Router("/user" -> userRoutes, "/operations" -> operationalRoutes).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
