package com.cloriko

import com.cloriko.protobuf.protocol.MasterRequest
import scala.language.implicitConversions

object MasterRequestUtils {

  case class OperationDescriptior(id: String, username: String, slaveId: String)

  def getMasterRequestDescriptor(masterRequest: MasterRequest): OperationDescriptior = {
    val sealedValue = masterRequest.sealedValue
    sealedValue.update match {
      case Some(update) => OperationDescriptior(update.id, update.username, update.slaveId)
      case _ =>
        sealedValue.delete match {
        case Some(delete) => OperationDescriptior(delete.id, delete.username, delete.slaveId)
        case _ =>
          sealedValue.fileRequest match {
            case Some(fileRequest) => OperationDescriptior(fileRequest.id, "fakeUsername", "fakeSlaveId")
            case _ =>
              sealedValue.overviewRequest match {
                case Some(fileRequest) => OperationDescriptior(fileRequest.id, fileRequest.username, "fakeSlaveId")
                case _ => OperationDescriptior("fakeId", "fakeUsername", "fakeSlaveId")
              }
          }
        }
      }
    }

  class ExtendedMasterRequest(masterRequest: MasterRequest) {
    private val descriptor = getMasterRequestDescriptor(masterRequest)
    val id: String = descriptor.id
    val username: String = descriptor.username
    val slaveId: String = descriptor.slaveId
  }

  implicit def extendMasterRequest(masterRequest: MasterRequest): ExtendedMasterRequest = {
    new ExtendedMasterRequest(masterRequest)
  }
}
