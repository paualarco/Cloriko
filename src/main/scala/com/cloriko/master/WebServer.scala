package com.cloriko.master

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.cloriko.master.grpc.GrpcServer
import org.http4s.implicits._
import org.http4s.server.blaze._
import com.cloriko.master.http.{OperationalRoutes, UserAuthRoutes}
import org.http4s.server.Router

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object WebServer extends IOApp with OperationalRoutes with UserAuthRoutes {

  val cloriko: Cloriko = new Cloriko

  val host = "0.0.0.0"
  val port = 8080

  val endPoint = s"http://localhost:$port"

  Future(new GrpcServer(endPoint, cloriko).start().blockUntilShutdown())

  val routes = userRoutes <+> operationalRoutes

  val httpApp = Router("/user" -> userRoutes, "/operations" -> operationalRoutes).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(port, "localhost")
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
