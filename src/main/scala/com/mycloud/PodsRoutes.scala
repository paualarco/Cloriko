package com.mycloud

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete


trait PodsRoutes extends JsonSupport {
  this: ActorSystemShape =>

  def userRegistryA: ActorRef


  lazy val podRoutes: Route =

    pathPrefix("pods" / Segment) {
      case username =>
        concat(
          path("join" / Segment) {
            case podId =>
              get {

                complete(s"Pod  joining to $username cloud")
              }
          },
          path("leave" / userPods) {
            get {
              userPods = userPods - username
              complete("pod leaving the cloud")
            }
          }
        )
    }
}
