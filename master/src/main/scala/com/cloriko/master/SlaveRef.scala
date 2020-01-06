package com.cloriko.master

import com.cloriko.master.Master.OperationId
import com.cloriko.master.grpc.GrpcServer.GrpcChannel
import com.cloriko.protobuf.protocol.{ Directory, FileReference, MasterRequest, SlaveResponse }
import com.cloriko.common.DecoderImplicits._

class SlaveRef(slaveId: String) {
  var snapshot: Directory = Directory("root", "root", "/", Seq[Directory](), Seq[FileReference]())
  var grpcChannel: Option[GrpcChannel[SlaveResponse, MasterRequest]] = None
  var pendingResponse: Map[OperationId, MasterRequest] = Map()

  def addPendingRequest(request: MasterRequest): this.type = {
    println(s"SlaveRef - Added pending update $request to slave ${request.slaveId}")
    pendingResponse = pendingResponse.updated(request.id, request.asProto)
    this
  }

  def removePendingResponse(id: String): this.type = {
    println(s"SlaveRef - Debug. Pending operations before removal: ${pendingResponse} ")
    pendingResponse = pendingResponse.-(id)
    println(s"SlaveRef - Debug. Pending operations after removal: ${pendingResponse} ")
    this
  }
}

object SlaveRef {
  def apply(slaveId: String): SlaveRef = new SlaveRef(slaveId)
}
