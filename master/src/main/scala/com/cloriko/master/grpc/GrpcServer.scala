package com.cloriko.master.grpc

import akka.actor.ActorSystem
import com.cloriko.protobuf.protocol.{ JoinReply, JoinRequest, _ }
import io.grpc.{ Server, ServerBuilder }
import monix.eval.Task
import monix.reactive.{ Observable, OverflowStrategy }
import akka.util.Timeout
import com.cloriko.master.Cloriko
import com.cloriko.master.grpc.GrpcServer.UpdateChannel
import monix.reactive.observers.Subscriber
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

class GrpcServer(localEndPoint: String, cloriko: Cloriko)(implicit actorSystem: ActorSystem) {

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
      //eval
      val JoinRequest(id, username, password, slaveId) = joinRequest
      cloriko.joinRequest(id, username, password, slaveId).map {
        case true => {
          println(s"GrpcServer - Returning JoinReply($id, true)")
          JoinReply(id, true)
        }
        case false => {
          println(s"GrpcServer - The $joinRequest failed")
          JoinReply(id, false)
        }
      }
    }

    override def updateStream(input: Observable[Updated]): Observable[Update] = {
      println("Grpc - UpdateStream protocol received!")
      val updatedDownstream = input
      Observable.create(OverflowStrategy.Unbounded) { updateUpStream: Subscriber.Sync[Update] =>
        updatedDownstream.runAsyncGetFirst.map { //Updated used for starting, this is also causing to trigger two observables
          case Some(Updated(id, username, slaveId)) => {
            println(s"Grpc - The first Update event of the flow was caught, username:$username, slaveId:$slaveId")
            cloriko.registerUpdateChannel(UpdateChannel(username, slaveId, updateUpStream, updatedDownstream)).runAsync
          }
          case None => println(s"Grpc - failed when getting first Updated event of the flow")
        }
      }
    }

    /*
      Observable.create(OverflowStrategy.Unbounded) { masterRequests: Subscriber.Sync[Update] =>
        input.runAsyncGetFirst.map{
          case Some(Updated(id, username, slaveId)) => {
              println(s"Grpc - The first Update event of the flow was caught, username:$username, slaveId:$slaveId")
              cloriko.registerUpdateChannel(UpdateChannel(username, slaveId, masterRequests, input)).runAsync
          }
          case None => println(s"Grpc - failed when getting first Updated event of the flow")
        }
      }
     */

    override def deleteStream(input: Observable[Deleted]): Observable[Delete] = {
      Observable.create(OverflowStrategy.Unbounded) { masterRequests: Subscriber.Sync[Delete] =>
        Task.now().runAsync
        // cloriko.registerUpdateChannel(UpdateChannel("paualarco", "slaveId-1", masterRequests, input)).runAsync
      }
    }

    override def overviewStream(input: Observable[Overview]): Observable[OverviewRequest] = {
      Observable.create(OverflowStrategy.Unbounded) { masterRequests: Subscriber.Sync[OverviewRequest] =>
        Task.now().runAsync
        // cloriko.registerUpdateChannel(UpdateChannel("paualarco", "slaveId-1", masterRequests, input)).runAsync
      }
    }

  }

  /* override def protocol(response: Observable[SlaveResponse]): Observable[MasterRequest] = {
      println(s"GrpcServer - Response received from slave! $response")
      //val masterRequest: MasterRequest = MasterRequest(MasterRequest.SealedValue.Update(Update("", "", None)))
      //Observable.fromIterable(1 to 10).map(_ => masterRequest).delayOnNext(1 seconds)

      Observable.create(OverflowStrategy.Unbounded) { masterRequests: Subscriber.Sync[MasterRequest] =>
        cloriko.registerSlaveChannel(SlaveChannel("paualarco", "slaveId-1", masterRequests, response)).runAsync
      }
    }
  }*/
}

object GrpcServer {
  sealed trait Channel
  case class UpdateChannel(username: String, slaveId: String, updateUpStream: Subscriber.Sync[Update], udatedDownStream: Observable[Updated]) extends Channel
  case class DeleteChannel(username: String, slaveId: String, masterRequests: Subscriber.Sync[Delete], slaveResponses: Observable[Deleted]) extends Channel
  case class OverviewChannel(username: String, slaveId: String, masterRequests: Subscriber.Sync[OverviewRequest], slaveResponses: Observable[Overview]) extends Channel

}

