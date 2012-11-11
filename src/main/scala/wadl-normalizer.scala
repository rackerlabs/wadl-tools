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

import com.rackspace.cloud.api.wadl.util.SVRLHandler

import scala.xml._

import java.io.InputStream
import java.io.Reader
import java.io.ByteArrayOutputStream

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._
import javax.xml.transform.dom._
import javax.xml.validation._

import org.xml.sax.XMLReader
import org.xml.sax.InputSource
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException

class WADLNormalizer(private var transformerFactory : TransformerFactory) {

  if (transformerFactory == null) {
    transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
  }

  try {
    //
    // Don't enable byte code generation in saxon-ee 9.4 this causes errors!
    //
    transformerFactory.setAttribute("http://saxon.sf.net/feature/generateByteCode",false)
  } catch {
    case i : IllegalArgumentException => { /* ignore */ }
  }

  //
  //  Setup validation factories to avoid clashes with saxon-ee
  //
  //
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema/saxonica", "com.saxonica.jaxp.SchemaFactoryImpl")
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory")
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema", "org.apache.xerces.jaxp.validation.XMLSchemaFactory")

  if (!transformerFactory.getFeature(SAXTransformerFactory.FEATURE)) {
    throw new RuntimeException("Need a SAX-compatible TransformerFactory!")
  }

  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
  private val wadlSchema = schemaFactory.newSchema(getClass().getClassLoader().getResource("wadl.xsd"))

  private val errorHandler : ErrorHandler = new Object() with ErrorHandler {
    def warning(e : SAXParseException) = {
      System.err.printf("[WARNING] %s (%s:%s:%s)\n",
                        e.getMessage(),
                        e.getSystemId(),
                        e.getLineNumber().toString(),
                        e.getColumnNumber().toString())
    }

    def error(e : SAXParseException) = throw e
    def fatalError(e : SAXParseException) = throw e
  }

  private val defaultResolver = transformerFactory.getURIResolver
  private val sourceMap = Map ( "normalizeWadl.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl.xsl")),
                               "normalizeWadl1.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl1.xsl")),
                               "normalizeWadl2.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl2.xsl")),
                               "normalizeWadl3.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl3.xsl")),
                               "normalizeWadl4.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl4.xsl")),
                               "normalizeWadl5.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl5.xsl")),
                               "wadl.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/wadl.xsl")))

  //
  //  Set input URL resolver
  //
  transformerFactory.setURIResolver (new Object() with URIResolver {
    def resolve(href : String, base : String) = sourceMap getOrElse (href, defaultResolver.resolve(href,base))
  })

  val saxTransformerFactory : SAXTransformerFactory = transformerFactory.asInstanceOf[SAXTransformerFactory]
  val templates : Templates = saxTransformerFactory.newTemplates(sourceMap("normalizeWadl.xsl"))

  //
  //  The schematron templates must use a private transformer factory.
  //
  val schematronTemplates : Templates = saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/wadl-links.xsl")))

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
  // Normalize a WADL given a source and result, this validates the
  // input WADL against the WADL schema and schematron rules. These
  // must all pass before the WADL is normalized.
  //
  def normalize(in: Source, out: Result,
                    format : Format,
                    xsdVersion : Version,
                    flattenXSDs : Boolean,
		              resource_types : ResourceType) : Unit = {

    //
    //  We purposly do the identity transform using xalan instead of
    //  Saxon, because of SaxonEE license issue.
    //
    val idTransform = TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl",null).newTransformer()
    val wadlResult = new DOMResult()

    //
    //  Transform WADL to DOM
    //
    idTransform.transform (in, wadlResult)
    val wadl = wadlResult.getNode()


    //
    //  Do Schematron template transformation, check for broken links
    //
    val schTransform = schematronTemplates.newTransformer
    val schResult = new SAXResult(new SVRLHandler)

    schTransform.transform (new DOMSource(wadl, in.getSystemId()), schResult)

    //
    //  Secondary check, do XSD transformation, fill in default values
    //
    val validWadlResult = new DOMResult()
    wadlSchema.newValidator().validate(new DOMSource(wadl, in.getSystemId()), validWadlResult)
    val validWadl = validWadlResult.getNode()

    //
    //  Perform the WADL normalization, on valid WADL
    //
    val transformer = newTransformer(format, xsdVersion, flattenXSDs, resource_types)
    transformer.transform (new DOMSource(validWadl, in.getSystemId()), out)
  }

  //
  // Normalize the WADL given the InputStream and a system ID to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : (String, InputStream), out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType) : Unit = {
    normalize (new StreamSource(in._2.asInstanceOf[InputStream],in._1.asInstanceOf[String]), out,
               format, xsdVersion, flattenXSDs,
               resource_types)
  }

  //
  // Normalize the WADL given the InputStream to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : InputStream, out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType) : Unit = {
    normalize (("test://test/mywadl.wadl",in), out, format, xsdVersion,
               flattenXSDs, resource_types)
  }

  //
  // Normalize the WADL given the Reader to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : Reader, out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType) : Unit = {
    normalize (new StreamSource(in), out,
               format, xsdVersion, flattenXSDs,
               resource_types)
  }

  //
  // Normalize the WADL given the SystemId to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : String, out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType) : Unit = {
    normalize (new StreamSource(in), out,
               format, xsdVersion, flattenXSDs,
               resource_types)
  }

  //
  // Normalize a WADL given a systemID and a NodeSeq, return a node
  // sequence response. The input WADL is validated against the WADL
  // xml schema.
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
  // Normalize a WADL given a NodeSeq and return a NodeSeq. The input
  // WADL is validated against the WADL xml schema.
  //
  def normalize(in : NodeSeq,
                format : Format = DONT,
                xsdVersion : Version = XSD11,
                flattenXSDs : Boolean = false,
		          resource_types : ResourceType = KEEP) : NodeSeq = {
    normalize(("test://test/mywadl.wadl", in), format, xsdVersion, flattenXSDs, resource_types)
  }

}
