package com.mycloud

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.mycloud.PodRegistry.{Join, PodInfo, PodRegistered, PodRegistryRequest}
import com.mycloud.UserRegistryActor.{GetUserInfo, UserAlreadyExisted, UserInfo}
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import com.mycloud.CloudMaster.ActionPerformed

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CloudMaster(userRegistry: ActorRef) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContext = context.dispatcher

  log.info("User registry was created!")
  implicit lazy val masterTimeout = Timeout(5.seconds)

  def receive: Receive = {
    case podRegistryRequest: PodRegistryRequest => {
      log.info("Sending GetUserInfo request")
      val future = userRegistry ? GetUserInfo(podRegistryRequest.username)
      future.onSuccess {
        case Some(userInfo: UserInfo) => {
          val futurePodRequest = userInfo.podRegistry ? podRegistryRequest
          futurePodRequest onSuccess { case _ =>
            log.info("Action performed")
            sender() ! ActionPerformed()
          }
        }
        case _ => log.error("Error message received when trying to get user info")
      }
    }


  }

}

object CloudMaster {
  def props(userRegistry: UserRegistryActor) = Props(classOf[CloudMaster], userRegistry)
  case class ActionPerformed()
}




