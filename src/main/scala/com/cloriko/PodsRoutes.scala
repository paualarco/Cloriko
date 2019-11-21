package com.cloriko

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives.{ path, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ get, post }
import akka.http.scaladsl.server.directives.RouteDirectives.complete
//import com.cloriko.cassandra.Cassandra
import com.cloriko.db.CassandraConfig

trait PodsRoutes extends JsonSupport {
  this: ActorSystemShape =>

  def userRegistryA: ActorRef

  val cassandraConfig = CassandraConfig(
    "localhost",
    9042,
    "admin",
    "admin",
    "cloriko",
    10,
    10)

  //val cassandra = new Cassandra(cassandraConfig)

  lazy val podRoutes: Route =

    pathPrefix("pods" / Segment) {
      case username =>
        concat(
          path("join" / Segment) {
            case podId =>
              get {
                println(s"Get received with username: $username and podId $podId")
                //cassandra.insertUserPodId(username)
                complete(s"Pod  joining to $username cloud")
              }
          }, path("query" / Segment) {
            podId =>
              //cassandra.getUserPods()
              complete("Cassandra query finished")
          },
          path("leave" / Segment) {
            case podId =>
              get {
                complete("pod leaving the cloud")
              }
          })
    }
}
