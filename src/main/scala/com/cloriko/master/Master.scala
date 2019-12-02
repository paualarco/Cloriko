package com.cloriko.master

import akka.actor.{Actor, ActorLogging, Props}
import com.cloriko.master.grpc.GrpcServer.SlaveChannel
import com.cloriko.protobuf.protocol._
import monix.eval.Task
class Master(username: String) {

  var slaves = Map[String, Slave]().empty //Map of (SlaveId -> (OperationId -> Operation))
  var numOfSlaves = slaves.size

  def registerSlave(slaveId: String): Task[Boolean] = {
    Task.eval {
      println("Registered slave")
      slaves = slaves.updated(slaveId, Slave(slaveId, Map(), None))
      true
    }
  }

  def updateSlaveChannel(slaveChannel: SlaveChannel): Task[Boolean] = {
    Task.eval{
      println(s"Received slave chanel at master from user: $username and slaveId: ${slaveChannel.slaveId}!")
      slaves.get(slaveChannel.slaveId) match {
        case Some(slave: Slave) => {
          println(s"Slave channel registered for slave ${slaveChannel.slaveId}")
          slaves = slaves.updated(slaveChannel.slaveId, slave.copy(channel = Some(slaveChannel)))
          true
        }
        case None => println(s"Slave ${slaveChannel.slaveId} not found"); false
      }
    }
  }

}
object Master {

  case class Slave(slaveId: String,
                       pendingOperations: Map[String, SlaveResponse],
                        channel: Option[SlaveChannel])


}

