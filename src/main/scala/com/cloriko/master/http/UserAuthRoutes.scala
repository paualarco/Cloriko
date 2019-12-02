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
import com.cloriko.master.UserAuthenticator.{UserAlreadyExisted, UserAuthenticated, UserCreated, UserNotExists, UserRejected}
import com.cloriko.master.WebServer.WebServer
import com.cloriko.master.http.UserJsonSupport.{LogInRequest, SignUpRequest}

trait UserAuthRoutes extends UserJsonSupport {
  this: WebServer =>

  def userAuthenticator: ActorRef

  implicit val timeout: Timeout // usually we'd obtain the timeout from the system's configuration

  lazy val userAuthRoutes: Route =
    concat(
      pathPrefix("user") {
        concat(
          path("signUp") {
            post {
              entity(as[SignUpRequest]) { signUpRequest =>
                println(s"User registry request received! $signUpRequest")
                val future = (userAuthenticator ? signUpRequest)
                onSuccess(future) {
                  case UserCreated(userName) => complete(StatusCodes.Created, s"The username $userName was created.")
                  case UserAlreadyExisted(userName) => complete(s"The username $userName was not created, it already existed.")
                }
              }
            }
          },
          path("logIn") {
            post {
              entity(as[LogInRequest]) { logInRequest =>
                println(s"User log in request received! $logInRequest")
                val future = (userAuthenticator ? logInRequest)
                onSuccess(future) {
                  case UserAuthenticated(userName) => complete(StatusCodes.Accepted, s"The user $userName logged in")
                  case UserNotExists(userName) => complete(StatusCodes.OK, s"The user $userName was rejected to log in with the given credentials")
                }
              }
            }
          })
      },
      path("user" / Segment / "public") { userName =>
        complete(s"$userName public cloud")
      })
}
