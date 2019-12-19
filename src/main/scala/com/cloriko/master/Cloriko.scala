package com.cloriko.master

import akka.util.Timeout
import com.cloriko.master.grpc.GrpcServer.GrpcChannel
import com.cloriko.protobuf.protocol.{MasterRequest, SlaveResponse, Update}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

class Cloriko {
  implicit lazy val timeout = Timeout(2 seconds)
  var masters: Map[String, Master] = Map() //username -> Master ActorRef

  def joinRequest(id: String, username: String, password: String, slaveId: String): Task[Boolean] = {
    val futureAuthentication = UserAuthenticator.authenticate(username, password)
    futureAuthentication.map {
      case true => {
        masters.get(username) match {
          case Some(master) => {
            println(s"Cloriko Info - Created master for user $username")
            master.registerSlave(slaveId).runAsync
          }
          case None => {
            println(s"Cloriko Debug - Master already existed for user $username")
            val master: Master = new Master(username)
            masters = masters.updated(username, master)
            master.registerSlave(slaveId).runAsync
          }
        }
        true //todo check if the slave was already part of the quorum
      }
      case false => {
        println(s"JoinRequest denied since username $username did not exist")
        false
      }
      case _ => {
        println(s"JoinRequest rejected since password was incorrenct username $username ")
        false
      }
    }
  }

  def registerChannel(slaveChannel: GrpcChannel[SlaveResponse, MasterRequest]): Task[Boolean] = {
    Task.eval {
      println("Cloriko - Slave chanel subscription received")
      masters.get(slaveChannel.username) match {
        case Some(master) => {
          println(s"Cloriko - Sending $slaveChannel from ${slaveChannel.slaveId} at master of ${slaveChannel.username}")
          master.registerChannel(slaveChannel).runAsync
          true
        }
        case None => println(s"Cloriko - Master not found for slaveChannel of ${slaveChannel.username} and ${slaveChannel.slaveId} "); false
      }
    }
  }

  def dispatchUpdateToMaster(updateOp: Update): Task[Boolean] = {
    masters.get(updateOp.username) match {
      case Some(master) => {
        println(s"Cloriko -  Update operation being sent to master of username: ${updateOp.username}")
        master.performUpdateOp(updateOp)
      }
      case None => {
        println(s"Cloriko - Update op of user ${updateOp.username} not delivered since master was not found")
        Task.now(false)
      }
    }
  }
}