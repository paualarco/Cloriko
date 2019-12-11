package com.cloriko.slave

import java.io.File

import com.cloriko.protobuf.protocol.{Directory, FileReference, File => SlaveFile}
import monix.execution.Scheduler.Implicits.global
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, GivenWhenThen, Matchers, WordSpecLike}
import org.scalatest.time.{Millis, Seconds, Span}
import com.cloriko._

import scala.concurrent.Future
import com.cloriko.slave.FileSystem.`./root`

class FileSystemSpec
  extends WordSpecLike
    with Matchers
    with ScalaFutures
    with GivenWhenThen
    with Generators
    with BeforeAndAfterAll {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(100, Millis)))



  "The creation of a directory" should {
    "perform given the Directory instance" in {
      //given a directory
      val dirCreate    = Directory("", "cloriko", "/create/test1/dirToCreate", Seq[Directory](), Seq[FileReference]())
      val dirNotCreate = Directory("", "cloriko", "/create/test1/dirNeverCreated", Seq[Directory](), Seq[FileReference]())

      //when
      val created: Boolean = FileSystem.createDir(dirCreate).runAsync.futureValue
      println(s"Created result: $created")
      //then
      created                     shouldEqual true

      File(dirCreate.absolutePath).exists     shouldEqual true
      File(dirNotCreate.absolutePath).exists  shouldEqual false

    }
    "perform given the absolute dir path" in {
      //given a directory
      val dirCreate    = `./root` + "/create/test2/dirToCreate"
      val dirNotCreate = `./root` + "/create/test2/dirNeverCreated"

      //when
      val created: Boolean = FileSystem.createDir(dirCreate)

      //then
      created                    shouldEqual true
      File(dirCreate).exists     shouldEqual true
      File(dirNotCreate).exists  shouldEqual false
    }
  }

  "The deletion of a directory" should {

    "be performed given the Directory instance" in {
      //given a directory
      val createdAndDeletedDir = Directory("", "cloriko", "/delete/test1/dirToDelete", Seq[Directory](), Seq[FileReference]())
      val dirNeverExisted      = Directory("", "cloriko", "/delete/test1/dirNeverExisted", Seq[Directory](), Seq[FileReference]())
      FileSystem.createDir(createdAndDeletedDir)

      //when
      val deletedExistingDir: Future[Boolean]    = FileSystem.deleteDir(createdAndDeletedDir).runAsync
      val deletedNonExistingDir: Future[Boolean] = FileSystem.deleteDir(dirNeverExisted).runAsync

      //then
      deletedExistingDir.futureValue                  shouldEqual true
      deletedNonExistingDir.futureValue               shouldEqual true
      File(createdAndDeletedDir.absolutePath).exists  shouldEqual false
      File(dirNeverExisted.absolutePath).exists       shouldEqual false
    }

    "be performed given the absolute dir path" in {
      //given a directory
      val createdAndDeletedDir = `./root` + "/delete/test1/dirToDelete"
      val dirNeverExisted      = `./root` + "/delete/test1/dirNeverExisted"
      val existedBeforeBeingCreated = File(createdAndDeletedDir).exists
      FileSystem.createDir(createdAndDeletedDir)

      //when
      val deletedExistingDir: Boolean    = FileSystem.delete(createdAndDeletedDir)
      val deletedNonExistingDir: Boolean = FileSystem.delete(dirNeverExisted)

      //then
      deletedExistingDir                 shouldEqual true
      deletedNonExistingDir              shouldEqual true
      existedBeforeBeingCreated          shouldEqual false
      File(createdAndDeletedDir).exists  shouldEqual false
      File(dirNeverExisted).exists       shouldEqual false
    }
  }

  "The creation of a file" should {

    "be performed given the File `SlaveFile` instance" in {
      //given a directory

      val fileToCreate: SlaveFile    = genSlaveFile()//.copy(fileName =  "fileToBeCreated.txt", "/delete/test1/dirToDelete", "Hello World!".getBytes())
      val fileToNotCreate: SlaveFile = genSlaveFile()
      FileSystem.createFile(fileToCreate)
      println(s"FileToCreate absolute path: ${fileToCreate.absolutePath}")
      //when
      val createdFile: Future[Boolean]    = FileSystem.deleteFile(fileToCreate).runAsync

      //then
      createdFile.futureValue                  shouldEqual true
      File(fileToCreate.absolutePath).exists  shouldEqual true
      File(fileToNotCreate.absolutePath).exists       shouldEqual false
    }

    "be performed given the absolute file path" in {
      //given a directory
      val createdAndDeletedDir = `./root` + "/delete/test1/dirToDelete"
      val dirNeverExisted      = `./root` + "/delete/test1/dirNeverExisted"
      val existedBeforeBeingCreated = File(createdAndDeletedDir).exists
      FileSystem.createDir(createdAndDeletedDir)

      //when
      val deletedExistingDir: Boolean    = FileSystem.delete(createdAndDeletedDir)
      val deletedNonExistingDir: Boolean = FileSystem.delete(dirNeverExisted)

      //then
      deletedExistingDir                 shouldEqual true
      deletedNonExistingDir              shouldEqual true
      existedBeforeBeingCreated          shouldEqual false
      File(createdAndDeletedDir).exists  shouldEqual false
      File(dirNeverExisted).exists       shouldEqual false
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    FileSystem.delete(FileSystem.`./root`)
    FileSystem.createDir(FileSystem.`./root`)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    FileSystem.delete(FileSystem.`./root`)
  }

  object File {
    def apply(path: String): File = new File(path)
  }
}
