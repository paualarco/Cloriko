package com.cloriko

import com.cloriko.protobuf.protocol.Update
import org.scalacheck.Gen

trait Generators {

  val genString: () => String = () => Gen.alphaStr.sample.get.substring(6)
  val genUpdate: Gen[Update] = Update(genString(), genString(), genString(), None)

}

