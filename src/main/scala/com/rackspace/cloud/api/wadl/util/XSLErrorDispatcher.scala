package com.rackspace.cloud.api.wadl.util

import org.xml.sax.SAXParseException

import scala.annotation.tailrec

trait XSLErrorDispatcher {
  //
  //  The error listener may send a SAXParseException, but newer
  //  versions of Saxon hide this in a RunTimeException, this call
  //  captures the error...traverses the cause and rethrows the
  //  SAXParseException.
  //
  def handleXSLException (f : => Any) : Unit = {
    try {
      f
    } catch {
      case e : RuntimeException => handleRTException(e)
      case t : Throwable => throw t
    }
  }

  @tailrec
  private def handleRTException(first : RuntimeException, e : Option[RuntimeException]=None) : Unit =
    e.getOrElse(first).getCause() match {
      case se : SAXParseException => throw se
      case rt : RuntimeException => handleRTException(first, Some(rt))
      case _ => throw first
  }
}
