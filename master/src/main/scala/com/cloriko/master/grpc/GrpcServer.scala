package com.cloriko.master.grpc

import com.cloriko.protobuf.protocol.{ JoinReply, JoinRequest, _ }
import com.cloriko.master.Gateway
import io.grpc.{ Server, ServerBuilder }
import monix.eval.Task
import monix.reactive.{ Observable, OverflowStrategy }
import com.cloriko.master.grpc.GrpcServer.GrpcChannel
import monix.reactive.observers.Subscriber
import monix.execution.Scheduler.Implicits.global
import com.cloriko.common.DecoderImplicits._
import scala.concurrent.duration._

class GrpcServer(localEndPoint: String, cloriko: Gateway) {

  private[this] var server: Server = null

  def start(): this.type = {
    server = {
      ServerBuilder
        .forPort(8980)
        .addService(
          ProtocolGrpcMonix.bindService(
            new MasterSlaveProtocolImpl(localEndPoint), // the service implemented above
            monix.execution.Scheduler.global))
        .build.start()
    }
    this
  }

  private def stop(): this.type = {
    if (server != null) server.shutdown()
    this
  }

  def blockUntilShutdown(): this.type = {
    if (server != null) server.awaitTermination()
    this
  }

  private class MasterSlaveProtocolImpl(localEndPoint: String) extends ProtocolGrpcMonix.MasterSlaveProtocol {
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

    override def protocol(input: Observable[SlaveResponse]): Observable[MasterRequest] = {
      println("Grpc - UpdateStream protocol received!")
      val downStream = input
      Observable.create(OverflowStrategy.Unbounded) { upStream: Subscriber.Sync[MasterRequest] =>
        downStream.runAsyncGetFirst.map { //Updated used for starting, this is also causing to trigger two observables
          case Some(slaveResponse: SlaveResponse) => {
            println(s"Grpc - The first Update event of the flow was caught, username:${slaveResponse.username}, slaveId:${slaveResponse.slaveId}")
            cloriko.registerGrpcChannel(GrpcChannel[SlaveResponse, MasterRequest](slaveResponse.username, slaveResponse.slaveId, downStream, upStream)).runAsync
          }
          case None => println(s"Grpc - failed when getting first Updated event of the flow")
        }
      }
    }
  }

}

object GrpcServer {
  case class GrpcChannel[D, U](username: String, slaveId: String, downStream: Observable[D], upStream: Subscriber.Sync[U])
}

