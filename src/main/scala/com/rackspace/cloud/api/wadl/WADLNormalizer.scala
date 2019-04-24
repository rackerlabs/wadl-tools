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

import java.io.{ByteArrayOutputStream, InputStream, Reader}

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.util.{EntityCatcher, LogErrorListener, XSLErrorDispatcher}
import com.typesafe.scalalogging.LazyLogging
import javax.xml.namespace.NamespaceContext
import javax.xml.transform._
import javax.xml.transform.dom._
import javax.xml.transform.sax._
import javax.xml.transform.stream._
import javax.xml.validation._
import javax.xml.xpath.{XPathConstants, XPathFactory}
import net.sf.saxon.jaxp.TransformerImpl
import net.sf.saxon.lib.NamespaceConstant
import org.w3c.dom.{Document, NodeList}
import org.xml.sax.helpers.XMLReaderFactory
import org.xml.sax.{SAXException, SAXParseException, XMLReader}

import scala.xml._

class WADLNormalizer(private var transformerFactory : TransformerFactory) extends LazyLogging with XSLErrorDispatcher {

  if (transformerFactory == null) {
    transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass.getClassLoader)
  }

  try {
    //
    // Don't enable byte code generation in saxon-ee 9.4 this causes errors!
    //
    transformerFactory.setAttribute("http://saxon.sf.net/feature/generateByteCode",false)
  } catch {
    case i : IllegalArgumentException => { logger.warn("Ignoring illegal argument when disabling generateByteCode in saxon", i) /* ignore */ }
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

  private val xpathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl", this.getClass.getClassLoader)
  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
  private val wadlSchema = schemaFactory.newSchema(getClass().getClassLoader().getResource("wadl.xsd"))

  private val defaultResolver = transformerFactory.getURIResolver
  private val sourceMap = Map ( "normalizeWadl.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl.xsl")),
                               "normalizeWadl1.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl1.xsl")),
                               "normalizeWadl2.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl2.xsl")),
                               "normalizeWadl3.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl3.xsl")),
                               "normalizeWadl4.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl4.xsl")),
                               "normalizeWadl5.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/normalizeWadl5.xsl")),
                               "wadl.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/wadl.xsl")),
                               "wadl-additional-reports.xsl" -> new StreamSource(getClass().getResourceAsStream("/xsl/wadl-additional-reports.xsl")))

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
  val svrlHandlerTemplates : Templates = saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/svrl-handler.xsl")))

  def this() = this(null)

  def newTransformer : Transformer = {
    val transformer = templates.newTransformer
    transformer.asInstanceOf[TransformerImpl].addLogErrorListener
    transformer
  }

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
  //  Create a reader with xinclude if possible
  //
  private def XMLReader(resolver : Option[EntityResolver] = None) : XMLReader = {
    val reader = XMLReaderFactory.createXMLReader()
    if (resolver != None) {
      reader.setEntityResolver(resolver.get)
    }
    try {
      reader.setFeature ("http://apache.org/xml/features/xinclude", true)
    } catch {
      case se: SAXException => logger.warn ("The XML parser does not seem to support XInclude! XIncludes will not be resolved!")
    }
    reader
  }

  //
  // Given a source, returns a source with the appropriate
  // EntityResolver.
  //
  private def Source(in : Source, resolver : Option[EntityResolver] = None) : Source = {
    val inputSource = SAXSource.sourceToInputSource(in)
    if (inputSource == null) {
      logger.warn (
        "I couldn't convert the source to a SAX stream, that means that if you used externaly defined "+
        "DTDs or entities I can't reliably report them as dependecies. Continuing to normalize WADL...")
      in
    } else {
      val ss = new SAXSource(inputSource)
      ss.setXMLReader(XMLReader(resolver))
      ss
    }
  }

  //
  //  Given an SVRL report, check refences that didn't look right in
  //  order to generate accurate error messages, we simply run each of
  //  these references through the XML parser to check them.
  //
  private val svrlAdditionalCheckExpression = {
    val xpath = xpathFactory.newXPath()
    xpath.setNamespaceContext(new Object() with NamespaceContext {
      def getNamespaceURI (prefix : String) = {
        if (prefix == "svrl") {
          "http://purl.oclc.org/dsdl/svrl"
        } else {
          null
        }
      }
      def getPrefix(uri : String) = null
      def getPrefixes(uri : String) = null
    })
    xpath.compile("/svrl:schematron-output/svrl:successful-report[@role='checkReference']/svrl:text")
  }

  private def checkAdditionalSVRLReports(doc : Document) = {
    val result = svrlAdditionalCheckExpression.evaluate (doc, XPathConstants.NODESET).asInstanceOf[NodeList]
    if (!result.isEmpty) {
      val reader = XMLReader()
      result.map(n => n.getTextContent().trim()).toSet.foreach((inDoc : String) => {
        try {
          reader.parse(inDoc)
          logger.warn (s"This is strange document $inDoc was reported for further checking, but looks good. Ignoring.")
        } catch {
          case spe : SAXParseException => logger.error (spe.toString())
                                          throw new SAXParseException(spe.toString(), null, spe)
          case e : Exception => logger.error (inDoc+" : "+e.getMessage())
                                throw new SAXParseException(inDoc+" : "+e.getMessage(), null, e)
        }
      })
    }
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
		              resource_types : ResourceType,
                    keepSCHReport : Boolean) : Unit = {

    //
    //  We purposly do the identity transform using xalan instead of
    //  Saxon, because of SaxonEE license issue.
    //
    val idTransform = TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl",this.getClass.getClassLoader).newTransformer()
    val entityCatcher = new EntityCatcher

    idTransform.setErrorListener (new LogErrorListener)

    //
    //  Transform WADL to DOM: We use the entityCatcher, which
    //  captures extenal entity dependecies along the way.
    //
    val wadl = new DOMResult()
    try {
      idTransform.transform (Source(in, Some(entityCatcher)), wadl)
    } catch {
      case spe : SAXParseException => logger.error (spe.getMessage())
                                      throw spe
      case e : Exception => logger.error (in.getSystemId()+" : "+e.getMessage())
                            throw new SAXParseException(in.getSystemId()+" : "+e.getMessage(), null, e)
    }

    //
    //  Do Schematron template transformation, check for broken links
    //
    val schTransform = schematronTemplates.newTransformer
    val schReport = new DOMResult()
    val svrlTransform = svrlHandlerTemplates.newTransformer
    val schEntityDoc = new DOMResult()

    schTransform.asInstanceOf[TransformerImpl].addLogErrorListener
    svrlTransform.asInstanceOf[TransformerImpl].addLogErrorListener

    //
    // Capture additional enitity references along the way.
    //
    schTransform.setURIResolver(new Object() with URIResolver {
      val origSCHURIResolver = schTransform.getURIResolver
      def resolve(href : String, base : String) = {
        Source(origSCHURIResolver.resolve(href, base), Some(entityCatcher))
      }
    })
    handleXSLException({
      schTransform.transform (new DOMSource(wadl.getNode, in.getSystemId()), schReport)
      checkAdditionalSVRLReports(schReport.getNode().asInstanceOf[Document])
      svrlTransform.setParameter ("systemIds", entityCatcher.systemIds.toArray[String])
      svrlTransform.transform (new DOMSource(schReport.getNode()), schEntityDoc)

      //
      //  Secondary check, do XSD transformation, fill in default values
      //
      val validWadlResult = new DOMResult()
      wadlSchema.newValidator().validate(new DOMSource(wadl.getNode, in.getSystemId()), validWadlResult)
      val validWadl = validWadlResult.getNode()

      //
      //  Perform the WADL normalization, on valid WADL
      //
      val transformer = newTransformer(format, xsdVersion, flattenXSDs, resource_types)
      if (keepSCHReport) {
        val normWADLResult = new DOMResult()
        transformer.transform (new DOMSource(validWadl, in.getSystemId()), normWADLResult)
        val wadlDocument = normWADLResult.getNode.asInstanceOf[Document]
        val wadlRoot = wadlDocument.getDocumentElement()
        val reportRoot = wadlDocument.importNode (schEntityDoc.getNode.asInstanceOf[Document].getDocumentElement, true)
        wadlRoot.appendChild(reportRoot)
        idTransform.transform(new DOMSource(wadlDocument), out)
      } else {
        transformer.transform (new DOMSource(validWadl, in.getSystemId()), out)
      }
    })
  }

  def normalize(in: Source, out: Result,
                    format : Format,
                    xsdVersion : Version,
                    flattenXSDs : Boolean,
		              resource_types : ResourceType) : Unit = {
    normalize(in, out, format, xsdVersion, flattenXSDs, resource_types, false)
  }

  //
  // Normalize the WADL given the InputStream and a system ID to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : (String, InputStream), out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType,
                keepSCHReport : Boolean) : Unit = {
    normalize (new StreamSource(in._2.asInstanceOf[InputStream],in._1.asInstanceOf[String]), out,
               format, xsdVersion, flattenXSDs,
               resource_types, keepSCHReport)
  }

  //
  // Normalize the WADL given the InputStream to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : InputStream, out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType,
                 keepSCHReport : Boolean) : Unit = {
    normalize (("test://test/mywadl.wadl",in), out, format, xsdVersion,
               flattenXSDs, resource_types, keepSCHReport)
  }

  //
  // Normalize the WADL given the Reader to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : Reader, out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType,
                keepSCHReport : Boolean) : Unit = {
    normalize (new StreamSource(in), out,
               format, xsdVersion, flattenXSDs,
               resource_types, keepSCHReport)
  }

  //
  // Normalize the WADL given the SystemId to the OUT Result. The
  // input WADL is validated against the WADL xml schema
  //
  def normalize(in : String, out: Result,
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType,
                keepSCHReport : Boolean) : Unit = {
    normalize (new StreamSource(in), out,
               format, xsdVersion, flattenXSDs,
               resource_types, keepSCHReport)
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
		          resource_types : ResourceType,
                keepSCHReport : Boolean) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    normalize(in, new StreamResult(bytesOut),
              format, xsdVersion, flattenXSDs,
              resource_types, keepSCHReport);
    XML.loadString (bytesOut.toString())
  }

  def normalize(in : (String, NodeSeq),
                format : Format,
                xsdVersion : Version,
                flattenXSDs : Boolean,
		          resource_types : ResourceType) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    normalize(in, new StreamResult(bytesOut),
              format, xsdVersion, flattenXSDs,
              resource_types, false);
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
		          resource_types : ResourceType = KEEP,
                keepSCHReport : Boolean = false) : NodeSeq = {
    normalize(("test://test/mywadl.wadl", in), format, xsdVersion, flattenXSDs, resource_types, keepSCHReport)
  }
}
