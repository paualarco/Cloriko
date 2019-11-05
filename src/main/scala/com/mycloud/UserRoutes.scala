package com.mycloud

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import UserRegistryActor._
import akka.http.scaladsl.model.StatusCodes

import scala.concurrent.duration._

trait UserRoutes extends JsonSupport {
  this: ActorSystemShape =>

  def userRegistryA: ActorRef

  implicit lazy val mycloTimeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val mycloRoutes: Route =
    concat(
    pathPrefix("login") {
      concat(
        get {
          complete("Hey, welcome! You are now in the login page.")
        },
        post {
          entity(as[UserRegistryRequest]) { userRegistryRequest =>
            println(s"User registry request received! $userRegistryRequest")
            val future = (userRegistryA ? userRegistryRequest)
            onSuccess(future) {
              case UserCreated(userName) => complete(StatusCodes.Created, s"The username $userName was created.")
              case UserAlreadyExisted(userName) => complete(s"The username $userName was not created, it already existed.")
            }
          }
        }

      )
    },
      path("user" / Segment / "public") { userName =>
        complete(s"$userName public cloud")
      }
    )
}
