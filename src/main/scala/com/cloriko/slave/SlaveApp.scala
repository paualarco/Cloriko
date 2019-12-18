package com.cloriko.slave

import cats.implicits._
import org.http4s.HttpRoutes
import cats.effect.{ ExitCode, IO, IOApp }
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future

object SlaveApp extends IOApp {

  val slaveRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "joinRequest" / username / password => {
      Ok(
        IO.fromFuture {
          IO {
            val slave = Slave(username)
            slave.joinRequestCloriko(password).runAsync.map {
              reply =>
                if (reply.authenticated) {
                  slave.initUpdateFlow(username, slave.slaveId)
                  //todo check that all flows were correctly initialized
                  s"Slave joined successfully and grpc protocol initialized with $username's cloriko."
                } else s"Slave - JoinRequest rejected from user $username"
            }
          }
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
