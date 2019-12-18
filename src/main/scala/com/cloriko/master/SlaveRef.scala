package com.cloriko.master

import com.cloriko.master.Master.{ OperationId }
import com.cloriko.master.SlaveRef.{ DeleteTrace, OverviewRequestTrace, UpdateTrace }
import com.cloriko.master.grpc.GrpcServer.{ DeleteChannel, OverviewChannel, UpdateChannel }
import com.cloriko.protobuf.protocol.{ Delete, Directory, FileReference, OverviewRequest, Update }

class SlaveRef(slaveId: String) {
  var snapshot: Directory = Directory("root", "root", "/", Seq[Directory](), Seq[FileReference]())
  var updateTrace = UpdateTrace()
  var deleteTrace = DeleteTrace()
  var overviewTrace = OverviewRequestTrace()

  def addPendingUpdate(update: Update): this.type = {
    println(s"SlaveRef - Added pending update $update to slave ${update.slaveId}")
    val updatedPendingUpdates = updateTrace.pendingUpdates.updated(update.id, update)
    updateTrace = updateTrace.copy(pendingUpdates = updatedPendingUpdates)
    this
  }

  def removePendingUpdate(id: String): this.type = {
    println(s"SlaveRef - Debug. Pending updateds before removal: ${updateTrace.pendingUpdates} ")
    val updatedPendingUpdates = updateTrace.pendingUpdates.-(id)
    updateTrace = updateTrace.copy(pendingUpdates = updatedPendingUpdates)
    println(s"SlaveRef - Debug. Pending updateds after removal: ${updateTrace.pendingUpdates} ")
    this
  }
}

object SlaveRef {
  def apply(slaveId: String): SlaveRef = new SlaveRef(slaveId)
  case class UpdateTrace(pendingUpdates: Map[OperationId, Update] = Map(), updateChannel: Option[UpdateChannel] = None)
  case class DeleteTrace(pendingDeletes: Map[OperationId, Delete] = Map(), deleteChannel: Option[DeleteChannel] = None)
  case class OverviewRequestTrace(pendingOverviewRequests: Map[OperationId, OverviewRequest] = Map(), overviewChannel: Option[OverviewChannel] = None)
  case class FileRequestTrace(pendingOverviewRequests: Map[OperationId, OverviewRequest] = Map(), overviewChannel: Option[OverviewChannel] = None)
  case class OverallTrace(updateTrace: UpdateTrace, deleteTrace: DeleteTrace, overviewTrace: OverviewRequestTrace)
}
