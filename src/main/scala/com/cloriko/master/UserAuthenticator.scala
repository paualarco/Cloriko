package com.cloriko.master

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cloriko.master.UserAuthenticator.{GetUserInfo, UserAlreadyExisted, UserAuthenticated, UserCreated, UserInfo, UserNotExists, UserRejected}
import com.cloriko.master.http.UserJsonSupport.{LogInRequest, SignUpRequest}

/*
  This class handles UserAutentication and UserRegistry
 */
class UserAuthenticator extends Actor with ActorLogging {

  var registeredUsersTable = Map[String, UserInfo]("paualarco" -> UserInfo("paualarco", "admin", "", "", ""))//.empty

  def receive: Receive = {
    case SignUpRequest(username, password, name, surname, email) => {
      if (registeredUsersTable.contains(username)) {
        log.info("User was already registered!")
        sender() ! UserAlreadyExisted(username)
      } else {
        log.info(s"Registering new user $username")
        val userInfo = UserInfo(username, password, name, surname, email)
        registeredUsersTable = registeredUsersTable.updated(username, userInfo)
        sender() ! UserCreated(username)
      }
    }
    case LogInRequest(username, password) => {
      registeredUsersTable.get(username) match {
        case Some(userInfo) => {
          if (userInfo.password equals password) {
            log.info(s"The user $username was authenticated")
            sender() ! UserAuthenticated(username)
          }
          else {
            log.info(s"The user $username was rejected")
            sender ! UserRejected(username)
          }
        }
        case None => {
          log.info(s"User $username does not exists, therefore user rejected")
          sender() ! UserNotExists(username)
        }
      }
    }
    case GetUserInfo(username) => {
      log.info(s"Pod registry actor request for user ${username}")
      val podRegistry = registeredUsersTable.get(username)
      sender() ! podRegistry
    }
  }

}

object UserAuthenticator {
  def props: Props = Props[UserAuthenticator]
  case class Great()
  case class UserInfo(userName: String, password: String, name: String, surname: String, email: String)
  case class GetUserInfo(username: String)

  sealed trait SignUpResponse
  case class UserAlreadyExisted(userName: String) extends SignUpResponse
  case class UserCreated(userName: String) extends SignUpResponse

  sealed trait LogInResponse
  case class UserAuthenticated(username: String) extends LogInResponse
  case class UserRejected(username: String) extends LogInResponse
  case class UserNotExists(username: String) extends LogInResponse

}
