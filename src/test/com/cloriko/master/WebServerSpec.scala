package com.cloriko.master

import com.cloriko.Generators
import com.cloriko.master.Master.OperationId
import com.cloriko.protobuf.protocol.Update
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.effect._
import com.cloriko.protobuf.protocol.FetchRequest
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Failure

class WebServerSpec extends FlatSpec with Matchers with ScalaFutures with Generators {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = 30.seconds, interval = 3.seconds)


 // val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8082/joinRequest/paualarco/admin"))


  val fetchEntity = """{"id":"testId", "username":"paualarco", "slaveId":"randomSlaveId", "fileName":"fileName1.yaml", "path":"/sample/path" }"""
  //val response: IO[Response[IO]] = WebServer.operationalRoutes.orNotFound.run(
  //  Request(method = Method.POST, uri = Uri.uri("/fetch") ).withEntity(fetchRequest)//FetchRequest("1234", "paualarco", "randomSlaveId", "fileName1.yaml", "/sample/path"))
  //)

  //Await.result(responseFuture, 10 seconds)

  val fetchRequest = HttpRequest(
    method = HttpMethods.POST,
    uri = "http://localhost:8080/fetch",
    entity = HttpEntity(ContentTypes.`application/json`, fetchEntity)
  )
  //val fetchReqFuture: Future[HttpResponse] = Http().singleRequest(fetchRequest)

  Thread.sleep(10)
  val fetchReqFuture1: Future[HttpResponse] = Http().singleRequest(fetchRequest)

  Await.result(fetchReqFuture1, 10 seconds)

  println("Response" + fetchReqFuture1)

}



