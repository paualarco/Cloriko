package com.cloriko.master

import monix.eval.Task

object UserAuthenticator {

  case class UserInfo(username: String, password: String, name: String, lastName: String, email: String)

  var registeredUsers = Map[String, UserInfo]("paualarco" -> UserInfo("paualarco", "admin", "", "", "")) //.empty

  def signUp(username: String, password: String, name: String, surname: String, email: String): Task[SignUpResult.SignUpResult] = {
    Task.eval {
      println(registeredUsers)
      registeredUsers.get(username) match {
        case Some(_: UserInfo) => {
          println("UserAuthenticator - The user already existed")
          SignUpResult.ALREADY_EXISTED
        }
        case None => {
          if(username.length < 5) {
            println(s"UserAuthenticator - SignUp rejected since username length was smaller than 5, username: $username")
            SignUpResult.REJECTED
          } else {
            println(s"UserAuthenticator - Registering new user $username")
            val userInfo = UserInfo(username, password, name, surname, email)
            registeredUsers = registeredUsers.updated(username, userInfo)
            SignUpResult.CREATED
          }
        }
      }
    }
  }

  def signIn(username: String, password: String): Task[SignInResult.SignInResult] = {
    Task.eval {
      registeredUsers.get(username) match {
        case Some(userInfo: UserInfo) => {
          if (userInfo.password equals password) {
            println(s"UserAuthenticator - The user $username was authenticated")
            SignInResult.AUTHENTICATED
          } else {
            println(s"UserAuthenticator- The user $username was rejected")
            SignInResult.REJECTED
          }
        }
        case None => {
          println(s"UserAuthenticator - User $username does not exists, therefore user rejected")
          SignInResult.USER_NOT_EXISTS
        }
      }
    }
  }

  object SignUpResult extends Enumeration {
    val CREATED, ALREADY_EXISTED, REJECTED = Value
    type SignUpResult = Value
  }

  object SignInResult extends Enumeration {
    val AUTHENTICATED, USER_NOT_EXISTS, REJECTED = Value
    type SignInResult = Value
  }
}

