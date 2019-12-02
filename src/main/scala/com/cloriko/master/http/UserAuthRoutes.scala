package com.cloriko.master.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, concat, entity, onSuccess, path, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import com.cloriko.master.MonixUserAuthenticator
import com.cloriko.master.WebServer.WebServer
import com.cloriko.master.http.UserJsonSupport.{LogInRequest, SignUpRequest}

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
trait UserAuthRoutes extends UserJsonSupport {
  this: WebServer =>


  implicit val timeout: Timeout // usually we'd obtain the timeout from the system's configuration

  lazy val userAuthRoutes: Route =
    concat(
      pathPrefix("user") {
        concat(
          path("signUp") {
            post {
              entity(as[SignUpRequest]) {
                case SignUpRequest(username, password, name, lastName, email) =>
                println(s"SingUpRequest received for user $username")
                val future = MonixUserAuthenticator.registerUser(username, password, name, lastName, email).runAsync
                onSuccess(future) {
                  case true => complete(StatusCodes.Created, s"The username $username was created.")
                  case false => complete(s"The username $username was not created, it already existed.")
                }
              }
            }
          },
          path("logIn") {
            post {
              entity(as[LogInRequest]) { case LogInRequest(username, password) =>
                println(s"User log in request received for user $username")
                val future = MonixUserAuthenticator.authenticate(username, password).runAsync
                onSuccess(future) {
                  case true => complete(StatusCodes.Accepted, s"The user $username logged in")
                  case false => complete(StatusCodes.OK, s"The user $username was rejected to log in with the given credentials")
                }
              }
            }
          })
      },
      path("user" / Segment / "public") { userName =>
        complete(s"$userName public cloud")
      })
}
