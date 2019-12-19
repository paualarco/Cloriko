package com.cloriko.master

import com.cloriko.master.grpc.GrpcServer.GrpcChannel
import com.cloriko.protobuf.protocol._
import monix.eval.Task
import monix.reactive.Consumer
import monix.execution.Scheduler.Implicits.global
import monix.reactive.observers.Subscriber
import Master.initialDir
import com.cloriko.DecoderImplicits._

class Master(username: String) {

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

  def sendAndPending(update: MasterRequest): Task[Boolean] = {
    Task.eval {
      slaves.get(update.slaveId) match {
        case Some(slave: SlaveRef) => {
          slave.grpcChannel match {
            case Some(channel) => {
              println(s"Master - Update operation sent to slave ${update.slaveId} of username: ${update.username}")
              slave.addPendingUpdate(update.asProto)
              channel.upStream.onNext(update.asProto)
              true
            }
            case None => {
              println(s"Master -  p of user ${update.username} not delivered since there was no update channel defined")
              false
            }
          }

        }
        case None => {
          println(s"Master - Update op of user ${update.username} not delivered since slaveId ${update.slaveId} was not found")
          false
        }
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
          slaveChannel.downStream.consumeWith(simplePendingOperationResponseConsumer)
          true
        }
        case None => println(s"Slave ${slaveChannel.slaveId} not found"); false
      }
    }
  }

  val simplePendingOperationResponseConsumer: Consumer.Sync[SlaveResponse, Unit] = {
    Consumer.foreach {
      updated => {
        println(s"Master - Consumed updated event $updated")
        slaves.get(updated.slaveId) match {
          case Some(slave) => slave.removePendingUpdate(updated.id)
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

