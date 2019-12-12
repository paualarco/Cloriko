package com.cloriko.slave

import java.io.{File, FileOutputStream}

import com.cloriko.protobuf.protocol.{Directory, FileReference, File => SlaveFile}
import com.google.protobuf.ByteString
import monix.eval.Task
import java.nio.file.{Files, Paths}

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

  def delete(file: File): Boolean = {
    val filePath: String = file.getPath
    if (filePath.startsWith(`./root`)) {
      if (!file.exists()) {
        println(s"FileSystem - The file $filePath that was supposed to be deleted, did not existed")
        false
      } else {
        val deleted = file.delete()
        println(s"FileSystem - Deleted $filePath, deleted response $deleted")
        deleted
      }
    } else {
      println(s"The file $filePath can not be deleted since there is no permissions to perform deletes outside of ${`./root`}")
      false
    }
  }

  def deleteDirRecursively(dir: File): Boolean = {
    val path = dir.getPath
    if (dir.getPath.contains(`./root`)) {
      if (dir.exists()) {
        var subFiles = dir.listFiles()
        if(subFiles==null) subFiles = Array()
        println(s"FileSystem - The directory $path to be deleted is not empty, deleting subdirectories first...")
        val subFilesDeleted = subFiles.foldLeft(true)((deleted: Boolean, subDir: File) => deleted  && deleteDirRecursively(subDir))
        //subFiles.foreach(_ => deleteDirRecursively(_))
        val deleted = dir.delete() && subFilesDeleted
        println(s"FileSystem - Deleted $path, recursive deletetion response $deleted")
        deleted
      } else {
        println(s"FileSystem - The path $path that was supposed to be deleted does not exist")
        false
      }
    } else {
      println(s"The dir $path since there is no permissions to perform deletes outside of ${`./root`}")
      false
    }
  }

  def deleteDir(directory: Directory): Task[Boolean] = {
    Task.eval {
      val dirPath: String = `./root` + directory.path + / + directory.dirName
      val dir = new File(dirPath)
      if (dir.isFile) {
        deleteDirRecursively(dir)
      } else { //todo test
        println(s"FileSyetem - The given slave file was not actually a file, path $dirPath ")
        false
      }
    }
  }

  def deleteFile(slaveFile: SlaveFile): Task[Boolean] = {
    Task.eval {
      val filePath = `./root` + slaveFile.path + / + slaveFile.fileId + "~" + slaveFile.fileName
      val file = new File(filePath)
      if(file.isFile){
        delete(file)
      } else { //todo test
        println(s"FileSyetem - The given slave file was not actually a file, path $filePath ")
        false
      }
    }
  }

  def scanFile(fileRef: FileReference): SlaveFile = {
    println("FileSystem - Scanning file")
    val bytes: Array[Byte] = Files.readAllBytes(Paths.get(fileRef.absolutePath))
    fileRef.asSlaveFile(ByteString.copyFrom(bytes))
  }

  //todo yet
  def moveDir(directory: Directory, newPath: String): Unit = ???

  def moveFile(file: FileReference, newPath: String): Unit = ???

  def replaceFile(oldFile: FileReference, newFile: File): Unit = ???

}
