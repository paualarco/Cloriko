package com.cloriko.master

import com.cloriko.master.Master.{DeleteTrace, OperationId, OverallTrace, OverviewTrace, Slave, UpdateTrace}
import com.cloriko.master.grpc.GrpcServer.{DeleteChannel, OverviewChannel, UpdateChannel}
import com.cloriko.protobuf.protocol._
import monix.eval.Task
import monix.reactive.{Consumer, Observable}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.observers.Subscriber
import Master.initialDir
import com.google.protobuf.ByteString

class Master(username: String) {

  val masterSnapshot: Directory = initialDir
  var slaves = Map[String, Slave]().empty //Map of (SlaveId -> (OperationId -> Operation))
  var numOfSlaves = slaves.size

  def registerSlave(slaveId: String): Task[Boolean] = {
    Task.eval {
      if (!slaves.contains(slaveId)) {
        println(s"Master - Registering slave $slaveId to the slave quorum")
        slaves = slaves.updated(slaveId, Slave(initialDir, OverallTrace(UpdateTrace(), DeleteTrace(), OverviewTrace())))
        true
      } else {
        println(s"Master - The slave $slaveId was already part of the slave quorum")
        false
      }
    }
  }

  def performUpdateOp(update: Update): Task[Boolean] = {
    Task.eval{
      slaves.get(update.slaveId) match {
        case Some(slave) => {
          slave.overallTrace.updateTrace.updateChannel match {
            case Some(updateChannel) => {
              println(s"MAster - Update operation sent to slave ${update.slaveId} of username: ${update.username}")
              updateChannel.masterRequests.onNext(update)
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
        case Some(slave: Slave) => {
          println(s"Master - A new slave channel registered for slave ${updateChannel.slaveId}")
          val updatedUpdateTrace: UpdateTrace = slave.overallTrace.updateTrace.copy(updateChannel = Some(updateChannel))
          val updatedOveralltrace: OverallTrace = slave.overallTrace.copy(updateTrace = updatedUpdateTrace)
          val updatedSlave: Slave = slave.copy(overallTrace = updatedOveralltrace)
          slaves = slaves.updated(updateChannel.slaveId, updatedSlave)
          consumeSlaveResponses(updateChannel.slaveResponses, updateChannel.masterRequests).runAsync
          true
        }
        case None => println(s"Slave ${updateChannel.slaveId} not found"); false
      }
    }
  }

  val fileExample = File("fileId-1", "fileName1", "/root", ByteString.copyFromUtf8("heey"))
  def operationResponse(masterRequests: Subscriber.Sync[Update]): Consumer[Updated, Unit] = {
    Consumer.foreach { _ => masterRequests.onNext(Update("update1", "paualarco", "slaveId-1", Some(fileExample))) }
    //Consumer.foreach { println }
  }

  def consumeSlaveResponses(slaveResponses: Observable[Updated], masterRequests: Subscriber.Sync[Update]) = {
    println(s"Master - DEBUG - Strating to consume from $slaveResponses and producing using $masterRequests")
    slaveResponses.consumeWith(printConsumer(masterRequests))// operationResponse(masterRequests))
  }


  val printConsumer: Subscriber.Sync[Update] => Consumer[Updated, Unit] = {
    updateUpstream =>
    Consumer.foreach(u => {
      println(s"Master - Consumed updated event $u")
      //updateUpstream.onNext(Update("update1", "paualarco", u.slaveId, None))
      slaves.get(u.slaveId).get.overallTrace.updateTrace.updateChannel.get.masterRequests.onNext(Update("update1", "paualarco", u.slaveId, None))
    })
  }

}

object Master {
  type OperationId = String
  val initialDir = Directory("root", "root", "/", Seq[Directory](), Seq[FileReference]())
  case class OverallTrace(updateTrace: UpdateTrace, deleteTrace: DeleteTrace, overviewTrace: OverviewTrace)
  case class Slave(snapshot: Directory, overallTrace: OverallTrace)
  case class UpdateTrace(pendingUpdates: Map[OperationId, Update] = Map(), updateChannel: Option[UpdateChannel] = None)
  case class DeleteTrace(pendingDeletes: Map[OperationId, Delete] = Map(), deleteChannel: Option[DeleteChannel] = None)
  case class OverviewTrace(pendingOverviewRequests: Map[OperationId, OverviewRequest] = Map(), overviewChannel: Option[OverviewChannel] = None)
  //todo in the future need to have a snapshot for each slave...

}

