package com.cloriko.master.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ concat, onSuccess, path, pathPrefix, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.cloriko.Generators
import com.cloriko.master.Cloriko
import com.cloriko.master.WebServer.WebServer
import com.cloriko.protobuf.protocol.Update
import monix.execution.Scheduler.Implicits.global

import scala.util.Random

trait OperationalRoutes extends UserJsonSupport with Generators {
  this: WebServer =>

  implicit val timeout: Timeout // usually we'd obtain the timeout from the system's configuration
  val cloriko: Cloriko

  lazy val operationalRoutes: Route =
    pathPrefix("operation") {
      concat(
        path("update") {
          get {
            val username = "paualarco"
            val randomId = Random.nextInt(100)
            val update = Update(s"$randomId", "paualarco", "randomSlaveId", Some(genSlaveFile()))
            println(s"WebServer - Update operation received by user ${username}")
            onSuccess(cloriko dispatchUpdateToMaster (update) runAsync) {
              case true => complete(StatusCodes.Created, s"The Update operation was delivered to randomSlaveId.")
              case false => complete(s"The update operation sent not delivered")
            }
          }
        })
    }
}
