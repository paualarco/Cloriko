package com.cloriko.master.http

import cats.effect.IO
import com.cloriko.DecoderImplicits._
import com.cloriko.master.Cloriko
import com.cloriko.protobuf.protocol.{ Delete, FetchRequest, File, FileReference, SlaveResponse, Update }
import monix.execution.Scheduler.Implicits.global
import org.http4s.HttpRoutes
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.{ ->, /, Ok, POST, Root }

import scala.util.Random
import cats.effect._
import com.google.protobuf.ByteString
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.multipart.Multipart
import cats.implicits._
import com.cloriko.Generators
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import monix.execution.CancelableFuture

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

trait OperationalRoutes extends Generators {

  val cloriko: Cloriko
  implicit val deleteDecoder = jsonOf[IO, Delete]
  implicit val fileReferenceDecoder = jsonOf[IO, FileReference]
  implicit val fetchRequestDecoder = jsonOf[IO, FetchRequest]

  lazy val operationalRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // case class com.cloriko.master.http.entities.DeleteEntity(id: String)

    case req @ POST -> Root / "multipart" / username / slaveId / fileName / encodedPath => {
      req.decode[Multipart[IO]] { multipart =>
        val parts = multipart.parts
        parts.find(_.name == "file".some) match {
          case Some(file) => {
            val byteString = ByteString.copyFrom(file.body.compile.toList.unsafeRunSync().toArray)
            val formattedPath = encodedPath.replace("|", "/")
            val update = Update("randomId", username, slaveId.toString, File(fileName, formattedPath, byteString).some)
            println(s"Multipart update received ${update}")
            val resp = cloriko.dispatchRequestToMaster(update.asProto).get //rElse(Task.now(s"The update request was not delivered"))
              .map {
                case _: SlaveResponse => s"The Update operation was performed on ${update.slaveId}."
              }.recoverWith {
                case e => Task.eval(s"The fetch request failed with error: $e").runAsync
              }
            Ok(IO.fromFuture(IO(resp)))
          }
          case None => BadRequest("The update multipart does not contain file")
        }
      }
    }

    case req @ POST -> Root / "delete" => {
      val delete: Delete = req.as[Delete].unsafeRunSync()
      println(s"WebServer - Delete operation received from user ${delete.username}")
      val resp = cloriko.dispatchRequestToMaster(delete.asProto).getOrElse(Task.now(s"The delete request sent was not delivered").runAsync)
        .map {
          case _: SlaveResponse => s"The delete operation was performed on ${delete.slaveId}."
        }.recoverWith {
          case e => Task.eval(s"The delete request failed with error: $e").runAsync
        }
      Ok(IO.fromFuture(IO(resp)))
    }

    case req @ POST -> Root / "fetch" => {
      val fetchRequest: FetchRequest = req.as[FetchRequest].unsafeRunSync()
      println(s"FetchRequest entity received: $fetchRequest")
      val request: Future[String] = cloriko.dispatchRequestToMaster(fetchRequest.asProto).get.map {
        slaveResponse => s"Slave response $slaveResponse"
      }
      request.foreach { fetch => println(s"Returning fetch response to user $fetch") }
      val result: String = IO.fromFuture { IO { request } }.unsafeRunSync()
      println(s"Final result $result")
      Ok(s"Result")
    }

    case req @ GET -> Root / "fetchGet" => {
      val fetchRequest: FetchRequest = genFetchRequest().copy(id = "randomId", username = "paualarco", slaveId = "randomSlaveId", fileName = "fileName1.yaml", path = "/sample/path")
      println(s"FetchRequest entity received: $fetchRequest")

      val request: Future[String] = cloriko.dispatchRequestToMaster(fetchRequest.asProto).get.map {
        slaveResponse => s"Slave response $slaveResponse"
      }
      //Await.result(request, 5 seconds)
      //.recoverWith { case e => Task.eval(s"The fetch request failed with error: $e").runAsync }
      request.foreach { fetch => println(s"Returning fetch response to user $fetch") }
      Ok { IO.fromFuture { IO { Task.eval[String]("HTTP RESPONSE").runAsync } } }
    }
  }
}

