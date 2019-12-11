package com

import com.cloriko.protobuf.protocol.{Directory, File}
import com.cloriko.slave.FileSystem.{`./root`, /}

import scala.language.implicitConversions

package object cloriko {

  implicit def dirUtils(dir: Directory): DirectoryUtils = {
    new DirectoryUtils(dir)
  }
  case class DirectoryUtils(directory: Directory) {
    val absolutePath: String = `./root` + directory.path + / + directory.dirName
  }

  implicit def fileUtils(file: File): FileUtils = {
    new FileUtils(file)
  }
  case class FileUtils(file: File) {
    val absolutePath: String = `./root` + file.path + `/` + file.fileId + "~" + file.fileName
  }


}
