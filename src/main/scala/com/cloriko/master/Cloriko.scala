package com.cloriko.master

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cloriko.protobuf.protocol.{Deleted, HeartBeat, JoinReply, JoinRequest, MasterRequest, SlaveResponse, Updated}
import akka.pattern.ask
import akka.util.Timeout
import com.cloriko.master.Cloriko.Greeter
import com.cloriko.master.Master.RegisterSlave
import com.cloriko.master.UserAuthenticator.{LogInResponse, UserAuthenticated, UserNotExists, UserRejected}
import com.cloriko.master.grpc.GrpcServer.SlaveChannel
import com.cloriko.master.http.UserJsonSupport.LogInRequest
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Cloriko(userAuthenticator: ActorRef) extends Actor with ActorLogging {
  implicit lazy val timeout = Timeout(2 seconds)
  var masters: Map[String, ActorRef] = Map() //username -> Master ActorRef

  def receive: Receive = {
    case JoinRequest(id, username, password, slaveId) => {
      val grpcHttpRef = sender() //the sender is not recongnised within onsuccess
      println(s"Sender: ${sender().path}")
      val futureAuthentication: Future[_] = (userAuthenticator ? LogInRequest(username, password)).mapTo[LogInResponse]
      futureAuthentication.onSuccess {
        //_ match {
          case _: UserAuthenticated => {
            log.info(s"JoinRequest accepted, master created for user: $username")
            val master: ActorRef = context.actorOf(Master.props(username), "master")
            masters = masters.updated(username, master)
            master ! RegisterSlave(slaveId)
            grpcHttpRef ! JoinReply(id, true)
            println(s"Sender: ${grpcHttpRef.path}")
          }
          case _: UserNotExists => {
            log.info(s"JoinRequest denied since username $username did not exist")
            grpcHttpRef ! JoinReply(id, false)
          }
          case _: UserRejected => {
            log.info(s"JoinRequest rejected since password was incorrenct username $username ")
            grpcHttpRef ! JoinReply(id, false)
          }
      }
    }

    case slaveChannel: SlaveChannel => {
      log.info("Slave chanel subscription received at Cloriko")
      masters.get(slaveChannel.username) match {
        case Some(master) => {
          log.info(s"Sending $slaveChannel from ${slaveChannel.slaveId} at master of ${slaveChannel.username}")
          master ! slaveChannel
        }
        case None => log.info(s"Master not found for slaveChannel of ${slaveChannel.username} and ${slaveChannel.slaveId} ")
      }
    }
    case Greeter() => println("Greeter received :)")
    case HeartBeat(username, _) => println("Heartbeat test received!")
    case Updated(_, username, _) =>
    case Deleted(_, username, _) =>
  }
}

object Cloriko {
  def props(userAuthenticator: ActorRef) = Props(new Cloriko(userAuthenticator))
  case class Greeter()
}
