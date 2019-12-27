package com.cloriko.master

import com.cloriko.master.grpc.GrpcServer.GrpcChannel
import com.cloriko.protobuf.protocol._
import monix.eval.Task
import monix.reactive.{ Consumer, Observable, OverflowStrategy }
import Master.initialDir
import com.cloriko.DecoderImplicits._
import com.cloriko.Generators
import monix.execution.CancelableFuture
import monix.reactive
import monix.reactive.observers.Subscriber
import monix.execution.Scheduler.Implicits.global

class Master(username: String) extends Generators {

  val masterSnapshot: Directory = initialDir
  var slaves = Map[String, SlaveRef]().empty //Map of (SlaveId -> (OperationId -> Operation))
  var numOfSlaves = slaves.size

  def registerSlave(slaveId: String): Task[Boolean] = {
    Task.eval {
      if (!slaves.contains(slaveId)) {
        println(s"Master - Registering slave $slaveId to the slave quorum")
        slaves = slaves.updated(slaveId, SlaveRef(slaveId))
        true
      } else {
        println(s"Master - The slave $slaveId was already part of the slave quorum")
        false
      }
    }
  }

  def consumerIdFilter(fetchResponseSubscriber: Subscriber.Sync[SlaveResponse], id: String) = {
    Consumer.foreach[SlaveResponse] {
      println(s"Master - Starting to consume filter with id: $id")
      slaveResponse =>
        println(s"Master - Slave response received $slaveResponse ")
        println(s"Master - DEBUG - Slave id ${slaveResponse.id} == id $id")
        if (slaveResponse.id == id) {
          println(s"Master - Fetch response caught at master with id: $id")
          fetchResponseSubscriber.onNext(slaveResponse)
        }
    }
  }

  def awaitFetchResponse(opId: String, slaveResponses: reactive.Observable[SlaveResponse]): CancelableFuture[SlaveResponse] = {
    println(s"Master - Awaiting fetch response with id $opId")
    var temporaryConsumer: CancelableFuture[Unit] = Task.now().runAsync
    val fetchResponseObservable: Observable[SlaveResponse] = Observable.create(OverflowStrategy.Unbounded) {
      fetchResponseSubscriber: Subscriber.Sync[SlaveResponse] =>
        temporaryConsumer = slaveResponses.consumeWith(consumerIdFilter(fetchResponseSubscriber, opId)).runAsync
        temporaryConsumer
    }
    val future = fetchResponseObservable.firstOrElseL(genFetchResponse().asProto)
    temporaryConsumer.cancel()
    future.runAsync
  }


  def sendRequest(request: MasterRequest): Option[CancelableFuture[SlaveResponse]] = {

    slaves.get(request.slaveId) match {
      case Some(slave: SlaveRef) => {
        slave.grpcChannel match {
          case Some(channel) => {
            println(s"Master - Update operation sent to slave ${request.slaveId} of username: ${request.username}")
            slave.addPendingRequest(request.asProto)
            val fetchResponse = awaitFetchResponse(request.id, channel.downStream)
            channel.upStream.onNext(request.asProto)
            Some(fetchResponse)
          }
          case None => {
            println(s"Master -  p of user ${request.username} not delivered since there was no update channel defined")
            None
          }
        }

      }
      case None => {
        println(s"Master - Update op of user ${request.username} not delivered since slaveId ${request.slaveId} was not found")
        None
      }
    }
  }

  def registerChannel(slaveChannel: GrpcChannel[SlaveResponse, MasterRequest]): Task[Boolean] = {
    Task.eval {
      println(s"Master - Received slave chanel at master from user: $username and slaveId: ${slaveChannel.slaveId}!")
      slaves.get(slaveChannel.slaveId) match {
        case Some(slave: SlaveRef) => {
          println(s"Master - A new slave channel registered for slave ${slaveChannel.slaveId}")
          slave.grpcChannel = Some(slaveChannel)
          println(s"Master - DEBUG - Strating to consume from ${slaveChannel.downStream} and producing using ${slaveChannel.upStream}")
          slaveChannel.downStream.consumeWith(simplePendingResponseConsumer)
          true
        }
        case None => println(s"Slave ${slaveChannel.slaveId} not found"); false
      }
    }
  }

  val simplePendingResponseConsumer: Consumer.Sync[SlaveResponse, Unit] = {
    Consumer.foreach {
      updated =>
        {
          println(s"Master - Consumed updated event $updated")
          slaves.get(updated.slaveId) match {
            case Some(slave) => slave.removePendingResponse(updated.id)

          }
        }
    }
  }

}

object Master {
  type OperationId = String
  val initialDir = Directory("root", "root", "/", Seq[Directory](), Seq[FileReference]())
  //todo in the future need to have a snapshot for each slave...
}

