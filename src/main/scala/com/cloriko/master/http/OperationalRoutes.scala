package com.cloriko.master.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, concat, onSuccess, path, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.cloriko.Generators
import com.cloriko.master.Cloriko
import com.cloriko.master.WebServer.WebServer
import com.cloriko.master.http.OperationalJsonSupport.{DeleteEntity, UpdateEntity}
import com.cloriko.master.http.UserJsonSupport.LogInRequest
import com.cloriko.protobuf.protocol.{Delete, FileReference, Update}
import monix.execution.Scheduler.Implicits.global

import scala.util.Random

trait OperationalRoutes extends UserJsonSupport with Generators {
  this: WebServer =>

  implicit val timeout: Timeout // usually we'd obtain the timeout from the system's configuration
  val cloriko: Cloriko

  lazy val operationalRoutes: Route =
    pathPrefix("operation") {
      concat(
        post {
          entity(as[UpdateEntity]) {
            case UpdateEntity(_, username, slaveId, fileId, fileName, path) =>
              val id = Random.nextInt(100)
              val update = Update(s"$id", username, slaveId, Some(genSlaveFile()))
              println(s"WebServer - Update operation received from user ${username}")
              onSuccess(cloriko dispatchUpdateToMaster (update) runAsync) {
                case true => complete(StatusCodes.Created, s"The Update operation was delivered to randomSlaveId.") // paualarco - randomSlaveId
                case false => complete(s"The update operation sent not delivered")
              }
          }
        },
        post {
          entity(as[DeleteEntity]) {
            case DeleteEntity(_, username, slaveId, fileId, fileName, path) =>
              val randomId = Random.nextInt(100)
              val delete = Delete(s"$randomId", username, "randomSlaveId", Seq(FileReference(fileId, fileName, path)))
              println(s"WebServer - Delete operation received from user ${username}")
              onSuccess(cloriko dispatchUpdateToMaster (update) runAsync) {
                case true => complete(StatusCodes.Created, s"The Update operation was delivered to randomSlaveId.")
                case false => complete(s"The update operation sent not delivered")
              }
          }
        }
      )
    }
}
