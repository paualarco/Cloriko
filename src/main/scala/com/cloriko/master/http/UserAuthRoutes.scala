package com.cloriko.master.http

import cats.effect.IO
import com.cloriko.DecoderImplicits._
import com.cloriko.master.{ Cloriko, UserAuthenticator }
import com.cloriko.protobuf.protocol.{ Delete, File, FileReference, Update }
import monix.execution.Scheduler.Implicits.global
import org.http4s.HttpRoutes
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.{ ->, /, Ok, POST, Root }

import scala.util.Random
import cats.effect._
import com.google.protobuf.ByteString
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.multipart.Multipart
import cats.implicits._
import com.cloriko.master.http.UserAuthRoutes.{ LogInRequestEntity, SignUpRequestEntity }
import monix.execution.Scheduler.Implicits.global

trait UserAuthRoutes {

  val cloriko: Cloriko
  implicit val signUpRequestEntityDecoder = jsonOf[IO, SignUpRequestEntity]
  implicit val logInRequestEntityDecoder = jsonOf[IO, LogInRequestEntity]

  lazy val userRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "signUp" => {
      val signUpRequest: SignUpRequestEntity = req.as[SignUpRequestEntity].unsafeRunSync()
      println(s"SignUp entity received: $signUpRequest")
      val SignUpRequestEntity(username, password, name, lastName, email) = signUpRequest
      println(s"WebServer - SingUpRequest received for user $username")
      val future = UserAuthenticator.registerUser(username, password, name, lastName, email).runAsync
        .map {
          case true => s"The username $username was created."
          case false => s"The username $username was not created, it already existed."
        }
      Ok(IO.fromFuture(IO(future)))
    }

    case req @ POST -> Root / "logIn" => {
      val logInRequestEntity: LogInRequestEntity = req.as[LogInRequestEntity].unsafeRunSync()
      println(s"LogIn entity received: $logInRequestEntity")
      val LogInRequestEntity(username, password) = logInRequestEntity
      println(s"WebServer - User log in request received for user $username")
      val future = UserAuthenticator.authenticate(username, password).runAsync
        .map {
          case true => s"The user $username logged in"
          case false => s"The user $username was rejected to log in with the given credentials"
        }
      Ok(IO.fromFuture(IO(future)))
    }

  }
}

object UserAuthRoutes {
  case class LogInRequestEntity(username: String, password: String)
  case class SignUpRequestEntity(userName: String, password: String, name: String, lastName: String, email: String)

}
