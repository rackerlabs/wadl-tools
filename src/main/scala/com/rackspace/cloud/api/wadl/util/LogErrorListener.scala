package com.rackspace.cloud.api.wadl.util

import com.typesafe.scalalogging.LazyLogging
import javax.xml.transform.{ErrorListener, TransformerException}
import org.xml.sax.SAXParseException


private object LogErrorListener {
  val trace   = "^\\[TRACE\\]\\s+(.*)".r
  val debug   = "^\\[DEBUG\\]\\s+(.*)".r
  val info    = "^\\[INFO\\]\\s+(.*)".r
  val warning = "^\\[WARNING\\]\\s+(.*)".r
  val error   = "^\\[ERROR\\]\\s+(.*)".r
  val se      = "^\\[SE\\]\\s+(.*)".r
}

import com.rackspace.cloud.api.wadl.util.LogErrorListener._

class LogErrorListener extends ErrorListener with LazyLogging {

  private def logException(e : TransformerException, default : => Unit) : Unit = {
    e.getMessage() match {
      case trace(m) => logger.trace(m)
      case debug(m) =>  logger.debug(m)
      case info(m) => logger.info(m)
      case warning(m) => logger.warn(m)
      case error(m) => logger.error(m)
      case se(m) => logger.error(m)
                    throw new SAXParseException (m, null)
      case s : String => default
    }
  }

  override def error (exception : TransformerException) : Unit =
    logException(exception, logger.error(exception.getMessage()))

  override def fatalError (exception : TransformerException) : Unit =
    logException(exception, logger.error(exception.getMessage()))

  override def warning (exception : TransformerException) : Unit =
    logException(exception, logger.warn(exception.getMessage()))

}
