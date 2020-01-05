package com.cloriko.master.http

import cats.effect.IO
import com.cloriko.master.WebServer
import com.cloriko.master.http.UserRoutes.{SignInEntity, SignUpEntity}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.circe._
import org.http4s.{Method, Request, Response, Uri}
import io.circe.syntax._
import org.http4s.implicits._

trait HttpFixture {
  implicit val signUpEncoder: Encoder[UserRoutes.SignUpEntity] = deriveEncoder
  implicit val signInEncoder: Encoder[UserRoutes.SignInEntity] = deriveEncoder

  def httpRequest(signInEntity: SignInEntity): Response[IO] = {
    WebServer.userRoutes.orNotFound.run(
      Request(method = Method.POST, uri = Uri.unsafeFromString("/signIn")).withEntity(signInEntity.asJson)
    ).unsafeRunSync()
  }

  def httpRequest(signUpEntity: SignUpEntity): Response[IO] = {
    WebServer.userRoutes.orNotFound.run(
      Request(method = Method.POST, uri = Uri.unsafeFromString("/signUp")).withEntity(signUpEntity.asJson)
    ).unsafeRunSync()
  }
}
