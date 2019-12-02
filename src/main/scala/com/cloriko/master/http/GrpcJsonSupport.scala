package com.cloriko.master.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol._
import GrpcJsonSupport.JoinRequest

trait GrpcJsonSupport extends SprayJsonSupport {
  implicit val joinRequest = jsonFormat4(JoinRequest)
}

object GrpcJsonSupport {
  case class JoinRequest(id: String, username: String, password: String, slaveId: String) {
    def toProto = com.cloriko.protobuf.protocol.JoinRequest(id, username, password, slaveId)
  }
}