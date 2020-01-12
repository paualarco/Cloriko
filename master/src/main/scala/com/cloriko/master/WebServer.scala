package com.cloriko.master

import cats.implicits._
import cats.effect.{ ExitCode, IO, IOApp }
import com.cloriko.master.grpc.GrpcServer
import org.http4s.implicits._
import org.http4s.server.blaze._
import com.cloriko.master.http.{ OperationalRoutes, UserRoutes }
import org.http4s.server.Router

import scala.concurrent.Future
import cats.implicits._
import org.http4s.HttpRoutes
import cats.effect.{ ExitCode, IO, IOApp }
import com.cloriko.config.MasterConfig
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import monix.execution.Scheduler.Implicits.global

object WebServer extends IOApp with OperationalRoutes with UserRoutes {

  val cloriko: Gateway = new Gateway

  val config: MasterConfig = MasterConfig.load()
  val host = config.server.host
  val port = config.server.port

  val endPoint = config.server.endPoint

  println(s"Starting web server on endpoint: $endPoint")

  Future(new GrpcServer(endPoint, cloriko).start().blockUntilShutdown())

  val routes = userRoutes <+> operationalRoutes

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
