package com.cloriko.protoexample

import java.util.logging.Logger

import akka.http.scaladsl.server
import com.cloriko.protobuf.protocol._
import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver
import monix.eval.Task
import monix.reactive.Observable

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object MonixMasterServer {
  private val logger = Logger.getLogger(classOf[HelloWorldServer].getName)

  def main(args: Array[String]): Unit = {
   val server = new MonixMasterServer()
    server.start()
    server.blockUntilShutdown()
  }

}

class MonixMasterServer() { self =>
  private[this] var server: Server = null

  private def start() = {
    //server = ServerBuilder.forPort(MasterServer.port).addService(ProtocolGrpc.bindService(new ProtocolImpl, executionContext)).build.start
    server = ServerBuilder
      .forPort(8980)
      .addService(
        ProtocolGrpcMonix.bindService(
          new MonixProtocolImpl(), // the service implemented above
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

  private class MonixProtocolImpl extends ProtocolGrpcMonix.Protocol {
    override def join(req: JoinRequest): Task[JoinReply] = {
      val reply = JoinReply(id = req.id)
      Task.eval(reply)
    }
    override def heartbeat(request: JoinReply): Observable[HeartBeat] = {
      val initially = request.id
      val source = Observable.interval(1.second)
      Observable.fromIterable(1 to 10)
        .map( int => HeartBeat(int.toString))
    }

  }

}
