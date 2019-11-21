package com.cloriko.protoexample

import java.util.logging.Logger

import com.cloriko.protobuf.protocol.{ HeartBeat, JoinReply, JoinRequest, ProtocolGrpc }
import io.grpc.stub.StreamObserver
import io.grpc.{ Server, ServerBuilder }

import scala.concurrent.{ ExecutionContext, Future }

object MasterServer {
  private val logger = Logger.getLogger(classOf[HelloWorldServer].getName)

  def main(args: Array[String]): Unit = {
    val server = new MasterServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }

  private val port = 50051
}

class MasterServer(executionContext: ExecutionContext) {
  self =>

  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder.forPort(MasterServer.port).addService(ProtocolGrpc.bindService(new ProtocolImpl, executionContext)).build.start

    MasterServer.logger.info("Server started, listening on " + MasterServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
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

  private class ProtocolImpl extends ProtocolGrpc.Protocol {
    override def join(req: JoinRequest) = {
      val reply = JoinReply(id = req.id)
      Future.successful(reply)
    }
    override def heartbeat(request: JoinReply, responseObserver: StreamObserver[HeartBeat]): Unit = ???
  }
}
