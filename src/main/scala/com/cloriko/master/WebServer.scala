package com.cloriko.master

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.cloriko.master.grpc.GrpcServer
import com.cloriko.master.http.{GrpcRoutes, UserAuthRoutes}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object WebServer {
  def main(args: Array[String]): Unit = {
    new WebServer
  }

  class WebServer extends UserAuthRoutes with GrpcRoutes {

    implicit val system: ActorSystem = ActorSystem("Cloriko")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout = Timeout(15 seconds)
    lazy val routes: Route = concat(userAuthRoutes, grpcRoutes)
    val userAuthenticator: ActorRef = system.actorOf(UserAuthenticator.props, "userRegistry")
    val cloriko: ActorRef = system.actorOf(Cloriko.props(userAuthenticator), "cloriko")

    val host = "0.0.0.0" //0.0.0.0
    val port = 8080

    val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, port)

    val endPoint = s"http://localhost:$port"
    Future(new GrpcServer(endPoint, cloriko).start().blockUntilShutdown())

    serverBinding.onComplete {
      case Success(bound) =>
        println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
      case Failure(e) =>
        Console.err.println(s"Server could not start!")
        e.printStackTrace()
        system.terminate()
    }

    Await.result(system.whenTerminated, Duration.Inf)
  }

}

