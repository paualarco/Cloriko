package com.cloriko.slave

import java.io.File

import com.cloriko.protobuf.protocol.{Directory, FileReference, File => SlaveFile}
import monix.execution.Scheduler.Implicits.global
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.time.{Millis, Seconds, Span}
import com.cloriko.{d, _}
import .`./root/data`
import com.google.protobuf.ByteString

import scala.concurrent.Future

class FileSystemSpec
  extends WordSpecLike
    with Matchers
    with ScalaFutures
    with Generators
    with BeforeAndAfterAll {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(100, Millis)))

  common.FileSystem.createDir(common.FileSystem.`./root/data`)


  "The slave FileSystem" should {
    "create of a directory" when {
      "a `Directory` instance is given" in {
        //given a directory
        val dirCreate = Directory("", "cloriko", "/create/test1/dirToCreate", Seq[Directory](), Seq[FileReference]())
        val dirNotCreate = Directory("", "cloriko", "/create/test1/dirNeverCreated", Seq[Directory](), Seq[FileReference]())

        //when
        val created: Boolean = common.FileSystem.createDir(dirCreate).runAsync.futureValue
        println(s"Created result: $created")

        //then
        created shouldEqual true
        File(dirCreate.absolutePath).exists shouldEqual true
        File(dirNotCreate.absolutePath).exists shouldEqual false
      }

      "the absolute dir path is given" in {
        //given a directory
        val dirCreate = `./root/data` + "/create/test2/dirToCreate"
        val dirNotCreate = `./root/data` + "/create/test2/dirNeverCreated"

        //when
        val created: Boolean = common.FileSystem.createDir(dirCreate)

        //then
        created shouldEqual true
        File(dirCreate).exists shouldEqual true
        File(dirNotCreate).exists shouldEqual false
      }
    }

    "delete a leaf directory" when {
      "a Directory instance is given" when {
        "" in {
          //given a directory
          val createdAndDeletedDir = Directory("", "cloriko", "/delete/test1/dirToDelete", Seq[Directory](), Seq[FileReference]())
          val dirNeverExisted = Directory("", "cloriko", "/delete/test1/dirNeverExisted", Seq[Directory](), Seq[FileReference]())
          common.FileSystem.createDir(createdAndDeletedDir)

          //when
          val deletedExistingDir: Future[Boolean] = common.FileSystem.deleteDir(createdAndDeletedDir).runAsync
          val deletedNonExistingDir: Future[Boolean] = common.FileSystem.deleteDir(dirNeverExisted).runAsync

          //then
          //     deletedExistingDir.futureValue                shouldEqual true
          deletedNonExistingDir.futureValue shouldEqual false //return false since the file did not existed
          File(createdAndDeletedDir.absolutePath).exists shouldEqual false
          File(dirNeverExisted.absolutePath).exists shouldEqual false
        }
      }

      "a `File` instance is given" in {
        //given a directory
        val createdAndDeletedDir = File(`./root/data` + "/delete/test2/dirToDelete")
        val existedBeforeBeingCreated = createdAndDeletedDir.exists
        common.FileSystem.createDir(createdAndDeletedDir.getPath())
        val dirNeverExisted = File(`./root/data` + "/delete/test2/dirNeverExisted")

        //when
        val deletedExistingDir: Boolean = common.FileSystem.deleteDirRecursively(createdAndDeletedDir)
        val deletedNonExistingDir: Boolean = common.FileSystem.deleteDirRecursively(dirNeverExisted)

        //then
        deletedExistingDir shouldEqual true
        deletedNonExistingDir shouldEqual false
        existedBeforeBeingCreated shouldEqual false
        createdAndDeletedDir.exists shouldEqual false
        dirNeverExisted.exists shouldEqual false
      }
    }

    "delete an intermediary `father` directory and all its sub directories and files" when {
      "a `Directory` instance is given" in {
        //given a directory
        val fatherDir = File(`./root/data` + "/delete/test3")
        common.FileSystem.createDir(fatherDir.getPath())
        val childDir = File(`./root/data` + "/delete/test3/childDir")
        common.FileSystem.createDir(childDir.getPath())
        val childFile = genSlaveFile().copy(fileName = "childFile.txt", path = "/delete/test3")
        common.FileSystem.createFile(childFile)

        //when
        val deleted = common.FileSystem.deleteDirRecursively(fatherDir)

        //then
        deleted shouldEqual true
        File(fatherDir.getPath).exists() shouldEqual false
        File(childDir.getPath()).exists() shouldEqual false
        File(childFile.absolutePath).exists() shouldEqual false
      }
    }

    "create a file" when {
      "a `SlaveFile` instance is given" in {
        //given a directory
        val fileToCreate: SlaveFile = genSlaveFile() //.copy(fileName =  "fileToBeCreated.txt", "/delete/test1/dirToDelete", "Hello World!".getBytes())
        val fileToNotCreate: SlaveFile = genSlaveFile()
        val existedBefore = File(fileToCreate.absolutePath).exists()

        //when
        common.FileSystem.createFile(fileToCreate).runAsync
        import java.nio.file.{Files, Paths}

        //then
        val content: ByteString = ByteString.copyFrom(Files.readAllBytes(Paths.get(fileToCreate.absolutePath)))
        existedBefore shouldEqual false
        File(fileToCreate.absolutePath).exists shouldEqual true
        File(fileToNotCreate.absolutePath).exists shouldEqual false
        content shouldEqual fileToCreate.data

      }
    }

    "The scan of a file" when {
      "performed given the File `SlaveFile` instance" in {
        //given a directory
        val initialFile: SlaveFile = genSlaveFile() //.copy(fileName =  "fileToBeCreated.txt", "/delete/test1/dirToDelete", "Hello World!".getBytes())
        common.FileSystem.createFile(initialFile).runAsync.futureValue

        //when
        val fileRef: FileReference = FileReference(initialFile.fileName, initialFile.path)
        val scannedFile = common.FileSystem.scanFile(fileRef).runAsync.futureValue

        //then
        scannedFile shouldEqual initialFile
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    common.FileSystem.deleteDirRecursively(File(common.FileSystem.`./root/data`))
    common.FileSystem.createDir(common.FileSystem.`./root/data`)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    common.FileSystem.deleteDirRecursively(File(common.FileSystem.`./root/data`))
  }

  object File {
    def apply(path: String): File = new File(path)
  }

}
