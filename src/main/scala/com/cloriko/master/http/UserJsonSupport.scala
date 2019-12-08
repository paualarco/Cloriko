package com.cloriko.master.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.cloriko.master.http.UserJsonSupport.{ LogInRequest, SignUpRequest }
import spray.json.DefaultJsonProtocol._

trait UserJsonSupport extends SprayJsonSupport {
  implicit val signUp = jsonFormat5(SignUpRequest)
  implicit val logIn = jsonFormat2(LogInRequest)
}

object UserJsonSupport {
  case class SignUpRequest(userName: String, password: String, name: String, lastName: String, email: String)
  case class LogInRequest(username: String, password: String)
}
