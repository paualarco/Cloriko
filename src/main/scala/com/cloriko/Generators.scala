package com.cloriko

import com.cloriko.master.UserAuthenticator.SignUpResult
import com.cloriko.master.http.UserAuthRoutes.{SignInEntity, SignUpEntity}
import com.cloriko.protobuf.protocol.{FetchRequest, FetchResponse, File, Update}
import com.google.protobuf.ByteString
import org.scalacheck.Gen

trait Generators {

  val genRange: Int => Int = range => Gen.chooseNum(1, range).sample.get //todo fix
  val genString = (n: Int) => Gen.listOfN(n, Gen.alphaChar).sample.get.mkString("")
  val genRand1000: () => String =  () => genRange(1000).toString //todo fix
  val genWithPrefixAndLen: (String, Int) => String = (prefix, lenght) => s"$prefix${genRand1000()}" //todo fix
  val genFileType = () => Gen.oneOf(List(".txt", ".json", ".xml")).sample.get
  val genWithPrefixIntAndSuffix = (prefix: String, lenght: Int, suffix: String) => s"$prefix${genRange(lenght)}$suffix" //todo fix

  val genUsername: () => String = () => genWithPrefixAndLen("user-", 100) //todo fix
  val genSlaveId: () => String = () => genWithPrefixAndLen("slave-", 100)
  val genFileId: () => String = () => genWithPrefixAndLen("file-", 100)
  val genFileName: () => String = () => genWithPrefixIntAndSuffix("fileName-", 100, genFileType())

  val genByteString: Int => ByteString = n => ByteString.copyFromUtf8(genString(n))
  val genUpdate: Gen[Update] = Update(genRand1000(), genUsername(), genSlaveId(), None)
  val generatePath: () => String = () => (1 to genRange(5)).foldLeft("") { case (path, _) => path + "/" + genString(7) }
  val genSlaveFile: () => File = () => File(genFileName(), generatePath(), genByteString(50))
  val genFetchResponse: () => FetchResponse = () => FetchResponse(genString(10), genUsername(), genSlaveId(), genFileName(), generatePath(), Some(genSlaveFile()))
  val genFetchRequest: () => FetchRequest = () => FetchRequest(genString(10), genUsername(), genSlaveId(), genFileName(), generatePath())
  val genSignInEntity: () => SignInEntity = () => SignInEntity(genUsername(), genString(10))
  val genSignUpEntity: () => SignUpEntity = () => SignUpEntity(genUsername(), genString(10), genString(7), genString(7), genString(10))
}
