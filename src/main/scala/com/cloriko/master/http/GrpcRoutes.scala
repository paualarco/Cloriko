package com.cloriko.master.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.pattern.ask
import com.cloriko.protobuf.protocol.JoinReply
import com.cloriko.master.http.GrpcJsonSupport.JoinRequest
import com.cloriko.master.WebServer.WebServer

trait GrpcRoutes extends GrpcJsonSupport {
  this: WebServer =>
  val cloriko: ActorRef

  implicit val timeout: Timeout // usually we'd obtain the timeout from the system's configuration

  lazy val grpcRoutes: Route =
    pathPrefix("grpc") {
      concat(
        path("joinRequest") {
          post {
            entity(as[JoinRequest]) { joinRequest =>
              println(s"Grpc http join request received! $joinRequest")
              onSuccess(cloriko ? joinRequest.toProto) {
                case joinReply: JoinReply => complete(StatusCodes.Created, s"Join reply '$joinReply' received from Cloriko")
                //case Greeter => println("Greeter test received at grpc http routes"); complete(StatusCodes.Accepted, s"Not created")
                case _ => complete(StatusCodes.Accepted, s"Not created")

              }
            }
          }
        }
      )
    }
}
