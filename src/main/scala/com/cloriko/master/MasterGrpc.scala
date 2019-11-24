package com.cloriko.master

import java.util.logging.Logger

import com.cloriko.protobuf.protocol._
import io.grpc.{ Server, ServerBuilder }
import com.cloriko.protobuf.protocol.JoinRequest
import com.cloriko.protobuf.protocol.{ JoinReply, OverviewRequest }
import monix.eval.Task
import monix.reactive.{ Observable, OverflowStrategy }
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

object MasterGrpc {
  private val logger = Logger.getLogger(classOf[MasterGrpc].getName)

  def main(args: Array[String]): Unit = {
    val server = new MasterGrpc()
    server.start()
    server.blockUntilShutdown()
  }
}

class MasterGrpc() { self =>
  private[this] var server: Server = null

  private def start() = {
    //server = ServerBuilder.forPort(MasterServer.port).addService(ProtocolGrpc.bindService(new ProtocolImpl, executionContext)).build.start
    server = ServerBuilder
      .forPort(8980)
      .addService(
        ProtocolGrpcMonix.bindService(
          new MasterSlaveProtocolImpl(), // the service implemented above
          monix.execution.Scheduler.global))
      .build.start()
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class MasterSlaveProtocolImpl extends ProtocolGrpcMonix.MasterSlaveProtocol {
    override def join(req: JoinRequest): Task[JoinReply] = {
      val reply = JoinReply(id = req.id)
      Task.eval(reply)
    }
    override def protocol(response: Observable[SlaveResponse]): Observable[MasterRequest] = {
      println(s"Response received from slave! $response")
      val masterRequest: MasterRequest = MasterRequest(MasterRequest.SealedValue.Update(Update("", "", None)))
      Observable.fromIterable(1 to 10).map(_ => masterRequest).delayOnNext(1 seconds)
      /*Observable.create(OverflowStrategy.Unbounded) { sub =>
        MonixFeeder.asyncProduceEvents(sub).runAsync
      }*/
    }
  }

}
