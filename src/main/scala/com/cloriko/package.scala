package com

import com.cloriko.protobuf.protocol.{ Directory, File, FileReference }
import com.cloriko.slave.FileSystem.{ /, `./root` }
import com.google.protobuf.ByteString

import scala.language.implicitConversions

package object cloriko {

  implicit def dirUtils(dir: Directory): DirectoryUtils = DirectoryUtils(dir)
  case class DirectoryUtils(directory: Directory) {
    val absolutePath: String = `./root` + directory.path + / + directory.dirName
  }

  implicit def fileUtils(file: File): FileUtils = FileUtils(file)
  case class FileUtils(file: File) {
    val absolutePath: String = `./root` + file.path + `/` + file.fileId + "~" + file.fileName
  }

  implicit def fileReferenceUtils(fileRef: FileReference): FileReferenceUtils = FileReferenceUtils(fileRef)
  case class FileReferenceUtils(fileRef: FileReference) {
    val absolutePath: String = `./root` + fileRef.path + `/` + fileRef.fileId + "~" + fileRef.fileName
    def asSlaveFile(data: ByteString): File = {
      File(fileRef.fileId, fileRef.fileName, fileRef.path, data)
    }
  }

}
