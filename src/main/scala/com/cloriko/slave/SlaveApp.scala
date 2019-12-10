package com.cloriko.slave

import cats.effect._
import cats.implicits._
import org.http4s.{ HttpRoutes, HttpService }
import org.http4s.syntax._
import org.http4s.dsl.io._
import cats.effect._
import org.http4s._, org.http4s.dsl.io._, org.http4s.implicits._
import org.http4s.server.blaze._
import monix.execution.Scheduler.Implicits.global

object SlaveApp extends IOApp {

  val slaveRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "joinRequest" / username / password => {
      Ok(
        IO.fromFuture {
          IO(
            Slave(username).joinRequestCloriko(password).runAsync.map {
              case true => {
                s"Slave joined successfully and grpc protocol initialized with $username's cloriko."
              }
              case false => {
                s"Slave joined successfully and grpc protocol initialized with $username's cloriko."
              }
            })
        })
    }
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8081, "localhost")
      .withHttpApp(slaveRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
