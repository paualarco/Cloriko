package com.cloriko.master

import com.cloriko.master.Master.OperationId
import com.cloriko.master.grpc.GrpcServer.GrpcChannel
import com.cloriko.protobuf.protocol.{Delete, Directory, FileReference, MasterRequest, OverviewRequest, SlaveResponse, Update}
import com.cloriko.DecoderImplicits._

class SlaveRef(slaveId: String) {
  var snapshot: Directory = Directory("root", "root", "/", Seq[Directory](), Seq[FileReference]())
  var grpcChannel: Option[GrpcChannel[SlaveResponse, MasterRequest]] = None
  var pendingOperations: Map[OperationId, MasterRequest] = Map()

  //var pendingUpdates: Map[OperationId, Update] = Map()
  //var pendingDeletes: Map[OperationId, Delete] = Map()
  //var pendingOverviewRequests: Map[OperationId, OverviewRequest] = Map()

  def addPendingUpdate(update: Update): this.type = {
    println(s"SlaveRef - Added pending update $update to slave ${update.slaveId}")
    pendingOperations = pendingOperations.updated(update.id, update.asProto)
    this
  }

  def removePendingUpdate(id: String): this.type = {
    println(s"SlaveRef - Debug. Pending operations before removal: ${pendingOperations} ")
    pendingOperations = pendingOperations.-(id)
    println(s"SlaveRef - Debug. Pending operations after removal: ${pendingOperations} ")
    this
  }
}

object SlaveRef {
  def apply(slaveId: String): SlaveRef = new SlaveRef(slaveId)
}
