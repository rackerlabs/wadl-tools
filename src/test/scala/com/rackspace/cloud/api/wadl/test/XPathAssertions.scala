package com.rackspace.cloud.api.wadl.test

import com.rackspace.cloud.api.wadl.Converters._
import javax.xml.namespace.NamespaceContext
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.xpath.{XPathConstants, XPathException, XPathFactory}
import net.sf.saxon.lib.NamespaceConstant
import org.scalatest.exceptions.TestFailedException

import scala.collection.mutable.{HashMap, Map}
import scala.xml.NodeSeq

trait XPathAssertions extends NamespaceContext {
  private val nsMap : Map[String, String] = new HashMap[String, String]()
  private val xpathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl", this.getClass.getClassLoader)


  def register (prefix : String, uri : String) : Unit = {
    nsMap += (prefix -> uri)
  }

  def assert (node : NodeSeq, xpathString : String) {
    try {
      val xpath = xpathFactory.newXPath()
      val src : Source = new StreamSource(node)

      xpath.setNamespaceContext(this);
      val xpathExpression = xpath.compile(xpathString)
      val ret : Boolean = xpathExpression.evaluate(src.asInstanceOf[Any], XPathConstants.BOOLEAN).asInstanceOf[Boolean]
      if (!ret) {
        throw new TestFailedException ("XPath expression does not evaluate to true(): "+xpathString+" "+node, 4)
      }
    } catch {
      case xpe : XPathException => throw new TestFailedException("Error in XPath! ", xpe, 4)
      case tf  : TestFailedException => throw tf
      case unknown : Throwable => throw new TestFailedException ("Unkown XPath assert error! "+node, unknown, 4)
    }
  }

  //
  //  Implementation of namespace context
  //
  def getNamespaceURI (prefix : String) = nsMap(prefix)
  def getPrefix(uri : String) = null
  def getPrefixes(uri : String) = null
}
