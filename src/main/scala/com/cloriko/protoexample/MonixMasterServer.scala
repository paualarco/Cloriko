package com.cloriko.protoexample

import java.util.logging.Logger

import akka.http.scaladsl.server
import com.cloriko.protobuf.protocol._
import io.grpc.{ Server, ServerBuilder }
import io.grpc.stub.StreamObserver
import monix.eval.Task
import monix.reactive.{ Observable, OverflowStrategy }
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
/*

object MonixMasterServer {
  private val logger = Logger.getLogger(classOf[MonixMasterServer].getName)

  def main(args: Array[String]): Unit = {
    val server = new MonixMasterServer()
    server.start()
    server.blockUntilShutdown()
  }

}
class MonixMasterServer() { self =>
  var server: Server = null

  def start() = {
    //server = ServerBuilder.forPort(MasterServer.port).addService(ProtocolGrpc.bindService(new ProtocolImpl, executionContext)).build.start
    server = ServerBuilder
      .forPort(8980)
      .addService(
        ProtocolGrpcMonix.bindService(
          new MonixProtocolImpl(), // the service implemented above
          monix.execution.Scheduler.global))
      .build.start()
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class MonixProtocolImpl extends Master {
    override def join(req: JoinRequest): Task[JoinReply] = {
      val reply = JoinReply(id = req.id)
      Task.eval(reply)
    }
    override def heartbeat(request: JoinReply): Observable[HeartBeat] = {
      val initially = request.id
      //Observable.fromIterable(1 to 10).map(int => HeartBeat(int.toString)).delayOnNext(1 seconds)
      Observable.create(OverflowStrategy.Unbounded) { sub =>
        MonixFeeder.asyncProduceEvents(sub).runAsync
      }
    }

  }

}
*/ 