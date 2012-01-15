package com.rackspace.cloud.api.wadl

object WADLFormat extends Enumeration {
  type Format = Value
  val TREE = Value("tree-format")
  val PATH = Value("path-format")
  val DONT = Value("dont-format")
}

object RType extends Enumeration {
  type ResourceType = Value
  val KEEP = Value("keep")
  val OMIT = Value("omit")
}

object XSDVersion extends Enumeration {
  type Version = Value
  val XSD10 = Value("1.0")
  val XSD11 = Value("1.1")
}

import WADLFormat._
import RType._
import XSDVersion._
import Converters._

import scala.xml._

import java.io.ByteArrayOutputStream

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

class WADLNormalizer(private var transformerFactory : TransformerFactory) {

  if (transformerFactory == null) {
    transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
  }

  if (!transformerFactory.getFeature(SAXTransformerFactory.FEATURE)) {
    throw new RuntimeException("Need a SAX-compatible TransformerFactory!")
  }

  private val defaultResolver = transformerFactory.getURIResolver
  private val sourceMap = Map ( "normalizeWadl.xsl" -> new StreamSource(getClass().getResourceAsStream("/normalizeWadl.xsl")),
                               "normalizeWadl1.xsl" -> new StreamSource(getClass().getResourceAsStream("/normalizeWadl1.xsl")),
                               "normalizeWadl2.xsl" -> new StreamSource(getClass().getResourceAsStream("/normalizeWadl2.xsl")),
                               "normalizeWadl3.xsl" -> new StreamSource(getClass().getResourceAsStream("/normalizeWadl3.xsl")),
                               "normalizeWadl4.xsl" -> new StreamSource(getClass().getResourceAsStream("/normalizeWadl4.xsl")))

  //
  //  Set input URL resolver
  //
  transformerFactory.setURIResolver (new Object() with URIResolver {
    def resolve(href : String, base : String) = sourceMap getOrElse (href, defaultResolver.resolve(href,base))
  })

  val saxTransformerFactory : SAXTransformerFactory = transformerFactory.asInstanceOf[SAXTransformerFactory]
  val templates : Templates = saxTransformerFactory.newTemplates(sourceMap("normalizeWadl.xsl"))

  def this() = this(null)

  def newTransformer : Transformer = templates.newTransformer

  def newTransformer(format : Format,
                     xsdVersion : Version,
                     flattenXSDs : Boolean,
		               resource_types : ResourceType) : Transformer = {
    val transformer = newTransformer

    transformer.setParameter("format",format.toString())
    transformer.setParameter("xsdVersion", xsdVersion.toString())
    transformer.setParameter("resource_types", resource_types.toString())
    transformer.setParameter("flattenXsds", flattenXSDs.toString())

    transformer
  }

  //
  // Normalize a WADL given a source and result
  //
  def normalize(in: Source, out: Result,
                    format : Format,
                    xsdVersion : Version,
                    flattenXSDs : Boolean,
		              resource_types : ResourceType) : Unit = {

    val transformer = newTransformer(format, xsdVersion, flattenXSDs, resource_types)
    transformer.transform (in, out)
  }

  //
  // Normalize a WADL given a systemID and a NodeSeq, return a node
  // sequence response.
  //
  def normalize(in : (String, NodeSeq),
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    normalize(in, new StreamResult(bytesOut),
              format, xsdVersion, flattenXSDs,
              resource_types);
    XML.loadString (bytesOut.toString())
  }

  //
  // Normalize a WADL given a NodeSeq and return a NodeSeq
  //
  def normalize(in : NodeSeq,
                format : Format = DONT,
                xsdVersion : Version = XSD11,
                flattenXSDs : Boolean = false,
		          resource_types : ResourceType = KEEP) : NodeSeq = {
    normalize(("", in), format, xsdVersion, flattenXSDs, resource_types)
  }

}
