package com.cloriko.master.grpc

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import com.cloriko.protobuf.protocol.{JoinReply, JoinRequest, _}
import io.grpc.{Server, ServerBuilder}
import monix.eval.Task
import monix.reactive.{Observable, OverflowStrategy}
import akka.util.Timeout
import com.cloriko.master.grpc.GrpcServer.SlaveChannel
import io.circe.generic.auto._
import io.circe.syntax._
import monix.reactive.observers.Subscriber
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Try

class GrpcServer(localEndPoint: String, cloriko: ActorRef)(implicit actorSystem: ActorSystem) {

  implicit lazy val timeout = Timeout(5.seconds)
  private[this] var server: Server = null

  def start(): this.type = {
    //server = ServerBuilder.forPort(MasterServer.port).addService(ProtocolGrpc.bindService(new ProtocolImpl, executionContext)).build.start
    server = ServerBuilder
      .forPort(8980)
      .addService(
        ProtocolGrpcMonix.bindService(
          new MasterSlaveProtocolImpl(localEndPoint), // the service implemented above
          monix.execution.Scheduler.global))
      .build.start()
    this
  }

  private def stop(): this.type = {
    if (server != null) {
      server.shutdown()
    }
    this
  }

  def blockUntilShutdown(): this.type = {
    if (server != null) {
      server.awaitTermination()
    }
    this
  }


  private class MasterSlaveProtocolImpl(localEndPoint: String)(implicit actorSystem: ActorSystem) extends ProtocolGrpcMonix.MasterSlaveProtocol {
    override def join(joinRequest: JoinRequest): Task[JoinReply] = {
      Task.eval{//eval
        val jsonJoinRequest = joinRequest.asJson.noSpaces
        println(s"Sending JoinRequest $jsonJoinRequest to $localEndPoint/grpc/joinRequest")
        val responseFuture: Future[HttpResponse] = Http()(actorSystem).singleRequest(
          HttpRequest(method = HttpMethods.POST,
            uri = s"$localEndPoint/grpc/joinRequest",
            entity = HttpEntity(ContentTypes.`application/json`, joinRequest.asJson.noSpaces))
        )
        val maybeResponse: Try[HttpResponse] = Try(Await.result(responseFuture, 5 seconds))
        val httpResult: HttpResponse = maybeResponse.getOrElse(HttpResponse(StatusCodes.NotFound))
        httpResult.status match {
          case StatusCodes.Created  => {
            println(s"The slave ${joinRequest.slaveId} was joined to the user ${joinRequest.username} cloud")
            JoinReply(joinRequest.id, true)
          }
          case StatusCodes.OK | _ => {
            println(s"The http join request $joinRequest was failed with status code ${httpResult.status}")
            JoinReply(joinRequest.id, false)
          }
        }
      }
    }

    override def protocol(response: Observable[SlaveResponse]): Observable[MasterRequest] = {
      println(s"Response received from slave! $response")
      //val masterRequest: MasterRequest = MasterRequest(MasterRequest.SealedValue.Update(Update("", "", None)))
      //Observable.fromIterable(1 to 10).map(_ => masterRequest).delayOnNext(1 seconds)
      Observable.create(OverflowStrategy.Unbounded) { masterRequests: Subscriber.Sync[MasterRequest] =>
        cloriko ! SlaveChannel("paualarco", "slaveId-1", masterRequests, response)
        Task.eval(println("Starting protocol at task eval")).runAsync
      }
    }
  }
}

object GrpcServer {
  case class SlaveChannel(username: String, slaveId: String, masterRequests: Subscriber.Sync[MasterRequest], slaveResponses: Observable[SlaveResponse])
}

