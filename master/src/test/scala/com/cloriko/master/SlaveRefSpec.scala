package com.cloriko.master

import com.cloriko.master.Master.OperationId
import com.cloriko.protobuf.protocol.Update
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

class SlaveRefSpec extends WordSpecLike with Matchers with ScalaFutures with Generators {

  "An new instance of SlaveRef" should {
    val slaveRef = SlaveRef("randomSlaveId")

    "add pending updates state" in {
      //given
      val updateOp = genUpdate.sample.get

      //when
      slaveRef.addPendingUpdate(updateOp)

      //then
      slaveRef.updateTrace.pendingUpdates shouldEqual Map(updateOp.id -> updateOp)
    }

    "remove existing updates from pendingUpdates" in {
      //given
      val updateOp = genUpdate.sample.get
      slaveRef.addPendingUpdate(updateOp)

      //when
      slaveRef.removePendingUpdate(updateOp.id)

      //then
      slaveRef.updateTrace.pendingUpdates shouldEqual Map[OperationId, Update]().empty
    }

  }
}
