package com.cloriko.slave

import java.io._
import java.io.File
import monix.eval.Task
import com.cloriko.protobuf.protocol.{Directory, FileReference, File => SlaveFile}
import monix.execution.Scheduler.Implicits.global

object FileSystem extends App {

  val `./`: String = "./"
  val `/`: String = "/"
  val rootDir = Directory("rootDir", "cloriko", "", Seq[Directory](), Seq[FileReference]())
  createDir(rootDir).runAsync

  def createDir(directory: Directory) = {
    Task.eval {
      val dirPath = `./` + directory.path + directory.dirName
      val dir: File = new File(`./` + directory.path + directory.dirName)
      if (!dir.exists()) {
        println(s"FileSystem - Created dir $dirPath ")
        dir.mkdir()
      } else {
        println(s"FileSystem - The dir $dirPath already existed")
        false
      }
    }
  }

  def createFile(slaveFile: SlaveFile): Task[Boolean] = {
    Task.eval{
      val filePath = `./` + slaveFile.path + `/` + slaveFile.fileName
      val file: File = new File(filePath)
      if(!file.exists()) {

      }
    }
  }

  def deleteDir(directory: Directory) = ???

  def deleteFile = ???

  def moveDir(directory: Directory, newPath: String): Unit = ???

  def moveFile(file: FileReference, newPath: String): Unit = ???
}
