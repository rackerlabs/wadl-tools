package com.rackspace.cloud.api.wadl.util

import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.Attributes
import org.xml.sax.SAXParseException
import org.xml.sax.Locator

class SVRLHandler extends DefaultHandler {
  private var buffer : StringBuilder = null
  private var lastText : String = null
  private var lastDoc : String = null

  override def characters (ch : Array[Char], start : Int, length : Int) : Unit = {
    if (buffer != null) {
      buffer.append (ch, start, length)
    }
  }

  override def startElement (uri : String, localName : String, qName : String,
                             attributes : Attributes) : Unit = {
    if (uri == "http://purl.oclc.org/dsdl/svrl") {
      localName match {
        case "text" => handleTextStart(uri, localName, qName, attributes)
        case "active-pattern" => handleActivePatternStart(uri, localName, qName, attributes)
        case _ => ; //Ignore
      }
    }
  }

  override def endElement (uri : String, localName : String, qName : String) : Unit = {
    if (uri == "http://purl.oclc.org/dsdl/svrl") {
      localName match {
        case "text" => handleTextEnd(uri, localName, qName)
        case "failed-assert" => handleFailedAssertEnd(uri, localName, qName)
        case _ => ; //ignore
      }
    }
  }

  private def handleTextStart(uri : String, localName : String, qName : String,
                             attributes : Attributes) : Unit = {
    buffer = new StringBuilder()
  }

  private def handleTextEnd(uri : String, localName : String, qName : String) : Unit = {
    lastText = buffer.toString().trim()
    buffer = null
  }

  private def handleActivePatternStart(uri : String, localName : String, qName : String,
                                     attributes : Attributes) : Unit = {
    lastDoc = attributes.getValue("document")
  }

  private def handleFailedAssertEnd(uri : String, localName : String, qName : String) : Unit = {
    if (lastDoc != null) {
      throw new SAXParseException (lastDoc + " : "+ lastText, null)
    } else {
      throw new SAXParseException (lastText, null)
    }
  }
}
