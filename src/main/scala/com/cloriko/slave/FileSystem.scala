package com.cloriko.slave

import java.io.{File, FileOutputStream}

import com.cloriko.protobuf.protocol.{Directory, FileReference, File => SlaveFile}
import monix.eval.Task
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
    if(dirPath.startsWith(`./root`)) {
      val dir: File = new File(dirPath)
      val created = dir.mkdirs() //it returns false if the file already existed
      if (created)  println(s"FileSystem - Created dir $dirPath ")
      else println(s"FileSystem - The dir $dirPath was already created")
      created
    } else {
      println(s"FileSystem - The dir was not created since the path $dirPath does not start by ${`./root`}")
      false
    }
  }

  def createFile(slaveFile: SlaveFile): Task[Boolean] = {
    Task.eval {
      val dirPath = `./root` + slaveFile.path
      val filePath = dirPath + `/` + slaveFile.fileId + "~" + slaveFile.fileName
      val file: File = new File(filePath)
      if (!file.exists()) {
        createDir(dirPath)
        Try {
          val outputStream: FileOutputStream = new FileOutputStream(filePath)
          outputStream.write(slaveFile.data.toByteArray)
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

  def delete(path: String): Boolean = {
    if(path.startsWith(`./root`)) {
      val file: File = new File(path)
      if (!file.exists()) {
        println(s"FileSystem - The path $path that was supposed to be deleted, did not existed")
        true
      } else {
        val deleted = file.delete()
        println(s"FileSystem - Deleted $path, deleted response $deleted")
        true
      }
    } else {
      println(s"The dir $path since there is no permissions to perform deletes outside of ${`./root`}")
      false
    }
  }

  def deleteDir(directory: Directory): Task[Boolean] = {
    Task.eval {
      val dirPath: String = `./root` + directory.path + / + directory.dirName
      delete(dirPath)
    }
  }

  def deleteFile(file: SlaveFile): Task[Boolean] = {
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
