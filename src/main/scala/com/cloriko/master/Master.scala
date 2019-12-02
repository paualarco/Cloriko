package com.cloriko.master

import akka.actor.{Actor, ActorLogging, Props}
import com.cloriko.master.Master.{RegisterSlave, Slave, SlaveRegistered}
import com.cloriko.master.grpc.GrpcServer.SlaveChannel
import com.cloriko.protobuf.protocol._
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

class Master(username: String) extends Actor with ActorLogging {


  var slaves = Map[String, Slave]().empty //Map of (SlaveId -> (OperationId -> Operation))
  var numOfSlaves = slaves.size
  log.info("User registry was created!")

  def receive: Receive = {
    case RegisterSlave(slaveId) => {
      log.info("Registered slave")
      slaves = slaves.updated(slaveId, Slave(slaveId, Map(), None))
      sender ! SlaveRegistered(slaveId)
    }
    case slaveChannel: SlaveChannel => {
      log.info(s"Received slave chanel at master from user: $username and slaveId: ${slaveChannel.slaveId}!")
      slaves.get(slaveChannel.slaveId) match {
        case Some(slave: Slave) => {
          log.info(s"Slave channel registered for slave ${slaveChannel.slaveId}")
          slaves = slaves.updated(slaveChannel.slaveId, slave.copy(channel = Some(slaveChannel)))
        }
        case None => log.error(s"Slave ${slaveChannel.slaveId} not found")
      }
    }
  }
}
object Master {
  def props(username: String): Props = Props(new Master(username))

  case class Slave(slaveId: String,
                       pendingOperations: Map[String, SlaveResponse],
                        channel: Option[SlaveChannel])

  case class RegisterSlave(slaveId: String)
  case class SlaveRegistered(slaveId: String)
}

