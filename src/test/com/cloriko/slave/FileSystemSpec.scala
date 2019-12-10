package com.cloriko.slave

import com.cloriko.protobuf.protocol.{Directory, FileReference, File => SlaveFile}
import com.cloriko.slave.FileSystem.createFile
import com.google.protobuf.ByteString
import monix.execution.Scheduler.Implicits.global
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import org.scalatest.time.{Millis, Seconds, Span}
import scala.concurrent.Future
import scala.util.Try

class FileSystemSpec extends FlatSpec with BeforeAndAfterAll with Matchers with ScalaFutures {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(100, Millis)))

  val cloriko = Directory("", "cloriko", "/fake/path", Seq[Directory](), Seq[FileReference]())
  //createDir(cloriko).runAsync
  val slaveFile = SlaveFile("1234", "fileName.txt", "/fake/path", ByteString.copyFromUtf8("Hello World"))
  createFile(slaveFile).runAsync

  "FileSystem" should "create a directory" in {
    //given
    val cloriko = Directory("", "cloriko", "/fake/path", Seq[Directory](), Seq[FileReference]())

    //when
    val created: Future[Boolean] = FileSystem.createDir(cloriko).runAsync

    //then
    created.futureValue shouldEqual true

  }
  override def beforeAll(): Unit = {
    super.beforeAll()
    FileSystem.createDir("/root/hello")
    FileSystem.delete(FileSystem.`./root`)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    FileSystem.createDir("/root/hello")

    FileSystem.delete(FileSystem.`./root`)
  }
}
