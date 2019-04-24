package com.rackspace.cloud.api.wadl.test

import java.net.URL

import com.rackspace.cloud.api.wadl.Converters._
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import org.scalatest.exceptions.TestFailedException
import org.xml.sax.SAXException

import scala.xml.NodeSeq

class SchemaAsserter(xsdSource : URL, useSaxon : Boolean = false) {
  private val factory = {
    if (useSaxon) {
      val inst = Class.forName("com.saxonica.ee.jaxp.SchemaFactoryImpl").newInstance.asInstanceOf[SchemaFactory]
      inst.setProperty("http://saxon.sf.net/feature/xsd-version","1.1")
      inst
    } else {
      val inst =  SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")
      //
      //  Enable CTA full XPath2.0 checking in XSD 1.1
      //
      inst.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)
      inst
    }
  }

  //
  // Create a schema
  //
  private val schema = factory.newSchema(xsdSource)

  def assert (node : NodeSeq) {
    try {
      val validator = schema.newValidator()
      validator.validate(new StreamSource(node))
    } catch {
      case se : SAXException => throw new TestFailedException("Validation Error on instance document: "+node, se, 4)
      case unknown : Throwable => throw new TestFailedException ("Unkown validation error! ", unknown, 4)
    }
  }
}
