package com.cloriko

import com.cloriko.protobuf.protocol.{File, Update}
import com.google.protobuf.ByteString
import org.scalacheck.Gen
trait Generators {

  val genRange: Int => Int = range => Gen.chooseNum(1, range).sample.get
  val genString = (n: Int) => Gen.listOfN(n, Gen.alphaChar).sample.get.mkString("")
  val genRand1000: String = genRange(1000).toString
  val genWithPrefixAndLen: (String, Int) => String = (prefix, lenght) => s"$prefix${genRange(lenght)}"
  val genUsername: () => String = () => genWithPrefixAndLen("user-", 100)
  val genSlaveId: () => String = () => genWithPrefixAndLen("slave-", 100)
  val genFileId: () => String = () => genWithPrefixAndLen("file-", 100)
  val genFileName: () => String = () => genWithPrefixAndLen("fileName-", 100)

  val genByteString: Int => ByteString = n => ByteString.copyFromUtf8(genString(n))
  val genUpdate: Gen[Update] = Update(genRand1000, genUsername(), genSlaveId(), None)
  val generatePath: () => String = () => (1 to genRange(5)).foldLeft("") { case (path, _) => path + "/" + genString(7) }
  val genSlaveFile: () => File = () => File(genFileId(), genFileName(), "", genByteString(50))
}

