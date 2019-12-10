package com.cloriko.slave

import java.io.{File, FileOutputStream}

import com.cloriko.protobuf.protocol.{Directory, FileReference, File => SlaveFile}
import com.google.protobuf.ByteString
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.util.{Failure, Success, Try}

object FileSystem {

  val `./root`: String = "./root"
  val `~` = "~"
  val `/` : String = "/"

  def createDir(directory: Directory): Task[Boolean] = {
    Task.eval {
      val dirPath = `./root` + directory.path + / + directory.dirName
      createDir(dirPath)
    }
  }

  def createDir(dirPath: String): Boolean = {
    val dir: File = new File(dirPath)
    if (!dir.exists()) {
      println(s"FileSystem - Created dir $dirPath ")
      dir.mkdirs()
    } else {
      println(s"FileSystem - The dir $dirPath already existed")
      false
    }
  }

  def createFile(slaveFile: SlaveFile): Task[Boolean] = {
    Task.eval {
      println()
      val dirPath = `./root` + slaveFile.path
      val filePath = dirPath + `/` + slaveFile.fileId + "~" + slaveFile.fileName
      val file: File = new File(filePath)
      if (!file.exists()) {
        createDir(dirPath)
        Try {
          val str = "Hello World!"
          val outputStream: FileOutputStream = new FileOutputStream(filePath)
          val strToBytes: Array[Byte] = str.getBytes()
          outputStream.write(strToBytes)
          outputStream.close()
        } match {
          case Failure(exception) => {
            println(s"FilesSystem - ERROR - The file ${filePath} could not be created, exception was caught: ${exception}")
            false
          }
          case Success(_) => {
            println(s"FileSystem - The file ${filePath} was created")
            true
          }
        }
      } else {
        println(s"The file ${slaveFile.fileName} already existed")
        false
      }
    }
  }

  //todo fix
  def delete(path: String): Boolean = {
    if(path!="/") {
      val file: File = new File(path)
      if (!file.exists()) {
        println(s"FileSystem - $path was alredy deleted")
        true
      } else {
        file.delete()
        println(s"FileSystem - Deleted $path")
        false
      }
    } else {
      println(s"FileSystem - No permission to remove path:$path ")
      false
    }
  }

  //todo fix
  def deleteDir(directory: Directory) = {
    Task.eval {
      val dirPath = `./root` + directory.path + / + directory.dirName
      delete(dirPath)
    }
  }

  //todo fix
  def deleteFile(file: SlaveFile) = {
    Task.eval {
      val dirPath = `./root` + file.path + / + file.fileId + "~" + file.fileName
      delete(dirPath)
    }
  }

  //todo

  def moveDir(directory: Directory, newPath: String): Unit = ???

  def moveFile(file: FileReference, newPath: String): Unit = ???

  def replaceFile(oldFile: FileReference, newFile: File): Unit = ???

}
