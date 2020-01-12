package com.cloriko.common.logging

import com.cloriko.common.DecoderImplicits.{ ExtendedMasterRequest, ExtendedSlaveResponse }
import com.typesafe.scalalogging.{ CanLog, LazyLogging, Logger, LoggerTakingImplicit }
import org.slf4j.MDC
import com.cloriko.protobuf.protocol.{ MasterRequest, SlaveResponse }

import scala.language.implicitConversions

trait ImplicitLazyLogger extends LazyLogging {

  implicit class ProductOps(p: Product) {
    def asMap: Map[String, String] = {
      val fields = p.getClass.getDeclaredFields.map(_.getName)
      val values = p.productIterator.toSeq.collect {
        case optional: Option[_] ⇒ optional
        case value ⇒ Option(value)
      }.flatten.map(_.toString)
      fields.zip(values).toMap
    }
  }

  protected implicit case object CanLogMasterRequest extends CanLog[ExtendedMasterRequest] {
    val extendedMasterRequestFields: Array[String] = classOf[ExtendedMasterRequest].getDeclaredFields.map(_.getName)
    override def logMessage(message: String, masterRequest: ExtendedMasterRequest): String = {
      masterRequest.asMap.foreach { case (key, value) ⇒ MDC.put(key, value) }
      message
    }
    override def afterLog(masterRequest: ExtendedMasterRequest): Unit =
      extendedMasterRequestFields.foreach(MDC.remove)
  }

  protected val ctxLogger: LoggerTakingImplicit[ExtendedMasterRequest] =
    Logger.takingImplicit[ExtendedMasterRequest](logger.underlying)

  implicit def update2LogCtx(implicit update: com.cloriko.protobuf.protocol.Update): ExtendedMasterRequest =
    new ExtendedMasterRequest(MasterRequest(MasterRequest.SealedValue.Update(update)))
  implicit def updated2LogCtx(implicit updated: com.cloriko.protobuf.protocol.Updated): ExtendedSlaveResponse =
    new ExtendedSlaveResponse(SlaveResponse(SlaveResponse.SealedValue.Updated(updated)))
  implicit def delete2LogCtx(implicit delete: com.cloriko.protobuf.protocol.Delete): ExtendedMasterRequest =
    new ExtendedMasterRequest(MasterRequest(MasterRequest.SealedValue.Delete(delete)))
  implicit def deleted2LogCtx(implicit deleted: com.cloriko.protobuf.protocol.Deleted): ExtendedSlaveResponse =
    new ExtendedSlaveResponse(SlaveResponse(SlaveResponse.SealedValue.Deleted(deleted)))
  implicit def fetchRequest2LogCtx(implicit fetchRequest: com.cloriko.protobuf.protocol.FetchRequest): ExtendedMasterRequest =
    new ExtendedMasterRequest(MasterRequest(MasterRequest.SealedValue.FetchRequest(fetchRequest)))
  implicit def fetchResponse2LogCtx(implicit fetchResponse: com.cloriko.protobuf.protocol.FetchResponse): ExtendedSlaveResponse =
    new ExtendedSlaveResponse(SlaveResponse(SlaveResponse.SealedValue.FetchResponse(fetchResponse)))

}

