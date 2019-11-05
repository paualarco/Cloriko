package com.mycloud

import akka.actor.{Actor, ActorLogging, Props}
import com.mycloud.PodRegistry.{Join, PodInfo, PodRegistered}
import com.mycloud.UserRegistryActor.{UserAlreadyExisted, UserCreated, UserInfo}

class PodRegistry extends Actor with ActorLogging {

  type JobId = String
  var podRegistry =  Map[JobId, PodInfo]().empty
  log.info("User registry was created!")

  def receive: Receive = {
    case Join(podInfo) => {
      if (podRegistry.contains(podInfo.podId)) {
        log.info("Pod was already registered!")
        sender() ! UserAlreadyExisted(podInfo.podId)
      }
      else {
        log.info(s"Registering new pod ${podInfo.podId}, for username: ${podInfo.username}")
        podRegistry = podRegistry.updated(podInfo.podId, podInfo)
        sender() ! PodRegistered(podInfo.podId)
      }
    }

  }

}
object PodRegistry {
  def props: Props = Props[PodRegistry]
  case class PodRegistryRequest(username: String)
  case class Join(podInfo: PodInfo) extends PodRegistryRequest(podInfo.username)
  case class PodInfo(override val username: String, podId: String, deviceType: String) extends PodRegistryRequest(username)
  sealed trait PodRegistryResponse
  case class PodAlreadyExisted(podId: String) extends PodRegistryResponse
  case class PodRegistered(podId: String) extends PodRegistryResponse
}



