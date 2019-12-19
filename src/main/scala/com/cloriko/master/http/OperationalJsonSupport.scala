package com.cloriko.master.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.cloriko.master.http.OperationalJsonSupport.{DeleteEntity, UpdateEntity}
import spray.json.DefaultJsonProtocol._

trait OperationalJsonSupport extends SprayJsonSupport {
  implicit val update = jsonFormat6(UpdateEntity)
  implicit val delete = jsonFormat6(DeleteEntity)
}

object OperationalJsonSupport {
  case class UpdateEntity(id: String, username: String, slaveId: String, fileId: String, fileName: String, path: String)
  case class DeleteEntity(id: String, username: String, slaveId: String, fileId: String, fileName: String, path: String)
}

