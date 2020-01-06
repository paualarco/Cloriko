package com.cloriko.slave

import cats.implicits._
import org.http4s.HttpRoutes
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import monix.execution.Scheduler.Implicits.global

object SlaveApp extends IOApp {

  println("Starting slave app")
  val slaveRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "joinRequest" / username / password => {
      val slave = Slave(username)
      val futureResponse = slave.joinRequestCloriko(password).runAsync.map {
        reply =>
          if (reply.authenticated) {
            slave.initProtocol(username, slave.slaveId)
            //todo check that all flows were correctly initialized
            s"Slave joined successfully and grpc protocol initialized with $username's cloriko."
          } else s"Slave - JoinRequest rejected from user $username"
      }
      Ok(IO.fromFuture(IO(futureResponse)))
    }

    case GET -> Root / "join" => {
      Ok(IO("futureResponse"))
    }
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8082, "localhost")
      .withHttpApp(slaveRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
