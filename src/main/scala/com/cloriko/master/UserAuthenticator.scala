package com.cloriko.master

import akka.actor.{Actor, ActorLogging, Props}
import com.cloriko.master.http.UserJsonSupport.{LogInRequest, SignUpRequest}

import monix.eval.Task

/*
  This class handles UserAutentication and UserRegistry
 */
object UserAuthenticator {

  case class UserInfo(username: String, password: String, name: String, lastName: String, email: String)

  var registeredUsers = Map[String, UserInfo]("paualarco" -> UserInfo("paualarco", "admin", "", "", ""))//.empty

  def registerUser(username: String, password: String, name: String, surname: String, email: String): Task[Boolean] = {
   Task.eval {
     registeredUsers.get(username) match {
       case Some(userInfo: UserInfo) => {
         println(s"Registering new user $username")
         val userInfo = UserInfo(username, password, name, surname, email)
         registeredUsers = registeredUsers.updated(username, userInfo)
         true
       }
       case None => {
         println("The user already existed")
         false
       }
     }
   }
  }

  def authenticate(username: String, password: String): Task[Boolean] = {
    Task.eval {
      registeredUsers.get(username) match {
        case Some(userInfo) => {
          if (userInfo.password equals password) {
            println(s"The user $username was authenticated")
            true
          }
          else {
            println(s"The user $username was rejected")
            false
          }
        }
        case None => {
          println(s"User $username does not exists, therefore user rejected")
          false
        }
      }
    }
  }
}






