package com.cloriko.slave

import com.cloriko.common.Global._
import com.cloriko.common.DecoderImplicits._

import com.cloriko.protobuf.protocol.{Delete, FetchRequest, FetchResponse, File, FileReference, JoinReply, JoinRequest, MasterRequest, ProtocolGrpcMonix, SlaveResponse, Update, Updated}
import io.grpc.ManagedChannelBuilder
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.observers.Subscriber
import monix.reactive.{Consumer, Observable, OverflowStrategy}
import com.cloriko.master.grpc.GrpcServer.GrpcChannel

class Slave(username: String) {
  val slaveId = "randomSlaveId"
  var grpcChannel: Option[GrpcChannel[MasterRequest, SlaveResponse]] = None

  val channel = ManagedChannelBuilder
    .forAddress("localhost", 8980)
    .usePlaintext(true)
    .build()

  val stub = ProtocolGrpcMonix.stub(channel) // only an async stub is provided

  def joinRequestCloriko(password: String): Task[JoinReply] = {
    println(s"Slave - Received join request at slave: $slaveId of user: $username")
    stub.join(JoinRequest("id1", username, password, slaveId))
  }

  def handleUpdate(update: Update, slaveUpStream: Subscriber.Sync[SlaveResponse]): Task[Unit] = {
    Task.eval {
      update.file match { //todo delete optional File field
        case Some(file) => {
          FileSystem.createFile(file).runAsync.onSuccess {
            case true => {
              slaveUpStream.onNext(Updated(update.id, username, slaveId).asProto)
              println(s"Slave - Update received: $update, returning Updated event, slaveResponseUpstream used: $slaveUpStream")
            }
            case false => println(s"There was a failure during the creation of file from update: ${update.id} at slave $slaveId")
          }
        }
        case None => {
          println(s"Slave - Update file ${update.id} was empty... dir to do for ${slaveId} ")
          slaveUpStream.onNext(Updated(update.id, username, slaveId).asProto)
        }
      }
    }
  }

  def handleDelete(delete: Delete, upStream: Subscriber.Sync[SlaveResponse]): Task[Unit] = {
    Task.eval {
      FileSystem.deleteFile(delete.references.head).runAsync.onSuccess {
        case true => {
          upStream.onNext(Updated(delete.id, username, slaveId).asProto)
          println(s"Slave - Update received: $delete, returning Updated event, slaveResponseUpstream used: $upStream")
        }
        case false => println(s"There was a failure during the creation of file from update: ${delete.id} at slave $slaveId")

      }
    }
  }

  def handleFetch(fetchRequest: FetchRequest, upStream: Subscriber.Sync[SlaveResponse]): Task[Unit] = {
    Task.eval {
      FileSystem.scanFile(FileReference(fetchRequest.fileName, fetchRequest.path)).runAsync.onSuccess {
        case file: File =>
          val fetchResponse = FetchResponse(fetchRequest.id, username, fetchRequest.slaveId, fetchRequest.fileName, fetchRequest.path, Some(file))
          upStream.onNext(fetchResponse.asProto)
          println(s"Slave - Fetch response sent: $fetchResponse to upStream channel: $upStream")
      }
    }
  }

  val masterRequestsConsumer: (Subscriber.Sync[SlaveResponse] => Consumer.Sync[MasterRequest, Unit]) = {
    upStream =>
      Consumer.foreach[MasterRequest] { masterRequest =>
        val sealedValue = masterRequest.sealedValue
        sealedValue.update match {
          case Some(update) => handleUpdate(update, upStream).runAsync
          case _ => sealedValue.delete match {
            case Some(deleteReq) => handleDelete(deleteReq, upStream).runAsync
            case _ => sealedValue.fetchRequest match {
              case Some(fetchRequest) => {
                handleFetch(fetchRequest, upStream).runAsync
              }
              case _ => println(s"Slave - A master request operation not yet implemented was received. sealedValue $sealedValue")

            }

          }
        }
      }
  }

  def initProtocol(username: String, slaveId: String) = {
    println(s"Slave - Initializing update flow from slave $slaveId to $username cluster")
    val emptyUpdated = Updated("0", username, slaveId)
    //val updateDownstream = stub.updateStream(Observable.fromIterable[Updated](List(updated)))

    val ob = stub.protocol {
      val obs = Observable.create[SlaveResponse](OverflowStrategy.Unbounded) {
        upStream: Subscriber.Sync[SlaveResponse] =>
          println(s"Slave - UpdatedUpStream created: $upStream and actioned")
          grpcChannel = Some(GrpcChannel[MasterRequest, SlaveResponse](username, slaveId, Observable.empty[MasterRequest], upStream))
          upStream.onNext(emptyUpdated.asProto)
          Task.now(println("Slave - Running dummy task")).runAsync
      }
      //obs.runAsyncGetFirst
      println(s"Slave - Update stream protocol called, observable created: $obs")
      obs
    }

    val consumerStarter: Task[MasterRequest] = ob.consumeWith(Consumer.head[MasterRequest])

    println("Checkpoint print")
    consumerStarter.runAsync.onSuccess {
      case _: MasterRequest => {
        val currentUpStream = grpcChannel.get.upStream
        println(s"Slave - Starting to consume update events from updateUpstream ${grpcChannel.get.upStream}")
        ob.consumeWith(masterRequestsConsumer(currentUpStream)).runAsync
        //ob.consumeWith(updateCousumer(currentUpstream)).runAsync
      }
      case _ => println("Slave - Update consumer not started... SOmething failed")
    }
    //    Await.result(obs.consumeWith(printConsumer).runAsync, 10 seconds)
  }
  //case class GrChannel(username: String, slaveId: String, updateUpstream: Option[Subscriber.Sync[SlaveResponse]], updateDownstream: Option[Observable[MasterRequest]])

}

object Slave {
  def apply(username: String): Slave = new Slave(username)
}
