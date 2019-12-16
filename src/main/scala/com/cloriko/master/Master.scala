package com.cloriko.master

import com.cloriko.master.Master.OperationId
import com.cloriko.master.grpc.GrpcServer.{ DeleteChannel, OverviewChannel, UpdateChannel }
import com.cloriko.protobuf.protocol._
import monix.eval.Task
import monix.reactive.{ Consumer, Observable }
import monix.execution.Scheduler.Implicits.global
import monix.reactive.observers.Subscriber
import Master.initialDir
import com.google.protobuf.ByteString

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

  def performUpdateOp(update: Update): Task[Boolean] = {
    Task.eval {
      slaves.get(update.slaveId) match {
        case Some(slave: SlaveRef) => {
          slave.updateTrace.updateChannel match {
            case Some(updateChannel) => {
              println(s"Master - Update operation sent to slave ${update.slaveId} of username: ${update.username}")
              slave.addPendingUpdate(update)
              updateChannel.updateUpStream.onNext(update)
              true
            }
            case None => {
              println(s"Master - Update op of user ${update.username} not delivered since there was no update channel defined")
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

  def registerUpdateChannel(updateChannel: UpdateChannel): Task[Boolean] = {
    Task.eval {
      println(s"Master - Received slave chanel at master from user: $username and slaveId: ${updateChannel.slaveId}!")
      slaves.get(updateChannel.slaveId) match {
        case Some(slave: SlaveRef) => {
          println(s"Master - A new slave channel registered for slave ${updateChannel.slaveId}")
          slave.updateTrace = slave.updateTrace.copy(updateChannel = Some(updateChannel))
          consumeSlaveResponses(updateChannel.udatedDownStream, updateChannel.updateUpStream).runAsync
          true
        }
        case None => println(s"Slave ${updateChannel.slaveId} not found"); false
      }
    }
  }

  val fileExample = File("fileId-1", "fileName1", "/root", ByteString.copyFromUtf8("heey"))
  def operationResponse(updateUpStream: Subscriber.Sync[Update]): Consumer[Updated, Unit] = {

    Consumer.foreach { _ => updateUpStream.onNext(Update("update1", "paualarco", "slaveId-1", Some(fileExample))) }
    //Consumer.foreach { println }
  }

  def consumeSlaveResponses(updatedDownStream: Observable[Updated], updateUpStream: Subscriber.Sync[Update]) = {
    println(s"Master - DEBUG - Strating to consume from $updatedDownStream and producing using $updateUpStream")
    updatedDownStream.consumeWith(printConsumer(updateUpStream)) // operationResponse(masterRequests))
  }

  val printConsumer: Subscriber.Sync[Update] => Consumer[Updated, Unit] = {
    updateUpstream =>
      Consumer.foreach(updated => {
        println(s"Master - Consumed updated event $updated")
        slaves.get(updated.slaveId) match {
          case Some(slave) => {
            slave.removePendingUpdate(updated.id)
            //slaves.get(updated.slaveId).get.updateTrace.updateChannel.get.updateUpStream.onNext(Update("update1", "paualarco", updated.slaveId, None))
          }
        }
        //updateUpstream.onNext(Update("update1", "paualarco", u.slaveId, None))
      })
  }

}

object Master {
  type OperationId = String
  val initialDir = Directory("root", "root", "/", Seq[Directory](), Seq[FileReference]())
  //todo in the future need to have a snapshot for each slave...

}

