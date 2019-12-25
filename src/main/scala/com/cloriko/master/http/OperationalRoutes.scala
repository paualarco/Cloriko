package com.cloriko.master.http

import cats.effect.IO
import com.cloriko.DecoderImplicits._
import com.cloriko.master.Cloriko
import com.cloriko.protobuf.protocol.{Delete, FetchRequest, File, FileReference, SlaveResponse, Update}
import monix.execution.Scheduler.Implicits.global
import org.http4s.HttpRoutes
import org.http4s.circe.jsonOf
import org.http4s.dsl.io.{->, /, Ok, POST, Root}

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
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import monix.execution.CancelableFuture

trait OperationalRoutes {

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
            val resp = cloriko.dispatchRequestToMaster(update.asProto).runAsync.map {
              case true => s"The Update operation was performed on ${update.slaveId}."
              case false => s"The update request was not delivered"
            }
            Ok(IO.fromFuture(IO(resp)))
          }
          case None => BadRequest("The update multipart does not contain file")
        }
      }
    }

    case req @ POST -> Root / "delete" => {
      val delete: Delete = req.as[Delete].unsafeRunSync()
      println(s"Delete entity received: $delete")
      val randomId = Random.nextInt(100)
      println(s"WebServer - Delete operation received from user ${delete.username}")
      val resp = cloriko.dispatchRequestToMaster(delete.asProto).runAsync.map {
        case true => s"The delete operation was performed on ${delete.slaveId}."
        case false => s"The delete request sent not delivered"
      }
      Ok(IO.fromFuture(IO(resp)))
    }

    case req @ POST -> Root / "fetch" => {
      val fetchRequest: FetchRequest = req.as[FetchRequest].unsafeRunSync()
      println(s"FetchRequest entity received: $fetchRequest")
      val resp = cloriko.dispatchFetchRequest(fetchRequest).runAsync.map[String] {
        case _: SlaveResponse => "Slave response"
        case _ => "Slave response not caught"
      }
      resp.foreach {
        slaveResponse =>
          println(s"Print fetch response on the screen $slaveResponse")
      }
      Ok(IO.fromFuture(IO(resp)))
    }
  }
}

