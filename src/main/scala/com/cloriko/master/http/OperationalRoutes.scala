package com.cloriko.master.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{concat, onSuccess, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import cats.effect.IO
import com.cloriko.DecoderImplicits._
import com.cloriko.master.Cloriko
import com.cloriko.protobuf.protocol.{Delete, File, FileReference, Update}
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

trait OperationalRoutes {

  val cloriko: Cloriko
  implicit val deleteDecoder = jsonOf[IO, Delete]
  implicit val fileReferenceDecoder = jsonOf[IO, FileReference]

  lazy val operationalRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      // case class com.cloriko.master.http.entities.DeleteEntity(id: String)

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

    case req @ POST -> Root / "multipart" / username / slaveId / fileId / fileName / encodedPath => {
        req.decode[Multipart[IO]] { multipart =>
          val parts = multipart.parts
          parts.find(_.name == "file".some) match {
            case Some(file) => {
              println("Multipart update succesfully received!")
              val byteString = ByteString.copyFrom(file.body.compile.toList.unsafeRunSync().toArray)
              println(s"Recevied data: ${byteString.toStringUtf8}")
              val formattedPath = encodedPath.replace("|", "/")
              val update = Update("randomId", username, slaveId.toString, File(fileId, fileName, formattedPath, byteString).some)
              println(s"Update created: ${update}")
              Ok(s"""Multipart Data\nParts:${multipart.parts.length}\n${multipart.parts.map(_.name).mkString("\n")}""")
            }
            case None => BadRequest("The update multipart does not contain file")
          }
        }
      }
    }
}

