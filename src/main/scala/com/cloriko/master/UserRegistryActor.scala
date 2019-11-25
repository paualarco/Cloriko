package com.cloriko

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import com.cloriko.UserRegistryActor.{ GetUserInfo, UserAlreadyExisted, UserCreated, UserInfo }

class UserRegistryActor extends Actor with ActorLogging {

  var registeredUsersTable = Map[String, UserInfo]().empty
  log.info("User registry was created!")

  def receive: Receive = {
    case UserRegistryRequest(userName, name, surname, email) =>
      if (registeredUsersTable.contains(userName)) {

        log.info("User was already registered!")
        sender() ! UserAlreadyExisted(userName)
      } else {
        log.info(s"Registering new user $userName")
        val podRegistry = context.actorOf(PodRegistry.props)
        registeredUsersTable = registeredUsersTable.updated(userName, UserInfo(userName, name, surname, email, podRegistry))
        sender() ! UserCreated(userName)
      }

    case GetUserInfo(username) =>
      log.info(s"Pod registry actor request for user ${username}")
      val podRegistry = registeredUsersTable.get(username)
      sender() ! podRegistry
  }

}

object UserRegistryActor {
  def props: Props = Props[UserRegistryActor]
  case class Great()
  case class UserInfo(userName: String, name: String, surname: String, email: String, podRegistry: ActorRef)
  case class GetUserInfo(username: String)
  sealed trait UserCreationResponse
  case class UserAlreadyExisted(userName: String) extends UserCreationResponse
  case class UserCreated(userName: String) extends UserCreationResponse
}
