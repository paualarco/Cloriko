package com.cloriko.common.logging

import com.cloriko.common.DecoderImplicits.ExtendedMasterRequest
import com.typesafe.scalalogging.{CanLog, LazyLogging, Logger, LoggerTakingImplicit}
import org.slf4j.MDC

import scala.language.implicitConversions

trait ImplicitLazyLogger extends LazyLogging {

  implicit class ProductOps(p: Product) {

    def asMap: Map[String, String] = {
      val fields = p.getClass.getDeclaredFields.map(_.getName)
      val values = p.productIterator.toSeq.collect {
        case optional: Option[_] ⇒ optional
        case value               ⇒ Option(value)
      }.flatten.map(_.toString)
      fields.zip(values).toMap
    }
  }

  implicit case object CanLogMasterRequest extends CanLog[ExtendedMasterRequest] {
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

}
