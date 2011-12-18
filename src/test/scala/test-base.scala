package com.rackspace.cloud.api.wadl.test

import scala.xml._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import javax.xml.transform._
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.namespace.NamespaceContext
import javax.xml.validation._
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathException
import java.io.File
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.apache.xml.security.c14n.Canonicalizer
import org.scalatest.FeatureSpec
import org.scalatest.Tag
import org.scalatest.TestFailedException
import org.xml.sax.SAXException
import net.sf.saxon.lib.OutputURIResolver
import net.sf.saxon.lib.NamespaceConstant

object WADLFormat extends Enumeration {
  type Format = Value
  val TREE = Value("tree-format")
  val PATH = Value("path-format")
  val DONT = Value("dont-format")
}

object XSDVersion extends Enumeration {
  type Version = Value
  val XSD10 = Value("1.0")
  val XSD11 = Value("1.1")
}

object Converters {
  //
  //  Convert a node sequence to a Source
  //
  implicit def nodeSeq2Source(ns : NodeSeq) : Source = new StreamSource(new ByteArrayInputStream(ns.toString().getBytes()))


  //
  //  Convert a node sequence string touple to a source with a system ID set
  //
  implicit def nodeSeqString2Source (nss : (String, NodeSeq)) : Source = {
    val s = nodeSeq2Source(nss._2)
    s.setSystemId(nss._1)
    s
  }

  //
  //  Convert a byte array stream result to a NodeSeq
  //
  implicit def byteArrayStreamResult2NodeSeq(sr : StreamResult) : NodeSeq = XML.loadString (sr.getOutputStream().toString())
}


import WADLFormat._
import XSDVersion._
import Converters._

class SchemaAsserter(xsdSource : String) {
  private val factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
  private val schema = factory.newSchema(new File(xsdSource))

  def assert (node : NodeSeq) {
    try {
      val validator = schema.newValidator()
      validator.validate(node)
    } catch {
      case se : SAXException => throw new TestFailedException("Validation Error: ", se, 4)
      case unknown => throw new TestFailedException ("Unkown validation error! ", unknown, 4)
    }
  }
}

trait XPathAssertions extends NamespaceContext {
  private val nsMap : Map[String, String] = new HashMap[String, String]()
  private val xpathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl", null)


  def register (prefix : String, uri : String) : Unit = {
    nsMap += (prefix -> uri)
  }

  def assert (node : NodeSeq, xpathString : String) {
    try {
      val xpath = xpathFactory.newXPath()
      val src : Source = node

      xpath.setNamespaceContext(this);
      val xpathExpression = xpath.compile(xpathString)
      val ret : Boolean = xpathExpression.evaluate(src.asInstanceOf[Any], XPathConstants.BOOLEAN).asInstanceOf[Boolean]
      if (!ret) {
        throw new TestFailedException ("XPath expression does not evaluate to true(): "+xpathString, 4)
      }
    } catch {
      case xpe : XPathException => throw new TestFailedException("Error in XPath! ", xpe, 4)
      case tf  : TestFailedException => throw tf
      case unknown => throw new TestFailedException ("Unkown XPath assert error! ", unknown, 4)
    } 
  }

  //
  //  Implementation of namespace context
  //
  def getNamespaceURI (prefix : String) = nsMap(prefix)
  def getPrefix(uri : String) = null
  def getPrefixes(uri : String) = null
}

trait TransformHandler {
  val transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)

  private val defaultResolver = transformerFactory.getURIResolver
  private val sourceMap : Map[String, Source] = new HashMap[String, Source]()
  private val destMap : Map[String, StreamResult] = new HashMap[String, StreamResult]()

  //
  //  Set input URL resolver
  //
  transformerFactory.setURIResolver (new Object() with URIResolver {
    //
    //  URL resolver implementation, I'm ignoring the base input, I
    //  don't think that we need it.
    //
    def resolve(href : String, base : String) = sourceMap getOrElse (href, defaultResolver.resolve(href, base)) 
  })

  //
  //  Set output URL resolver
  //
  transformerFactory.setAttribute ("http://saxon.sf.net/feature/outputURIResolver", new Object() with OutputURIResolver {
    //
    //  Output URI resolver, again ignoring the base here...
    //
    def resolve(href : String, base : String) = {
      val result = new StreamResult(new ByteArrayOutputStream())
      destMap += (href -> result)
      result
    }

    //
    //  Close the result
    //
    def close(result : Result) = {
      result.asInstanceOf[StreamResult].getOutputStream().close()
    }
  })

  //
  //  Add a source to consider
  //
  def register (url : String, xml : NodeSeq) : Unit = {
    sourceMap += (url -> xml)
  }

  def register (in : (String, NodeSeq)) : Unit = register(in._1, in._2)

  //
  //  Get outputs
  //
  def outputs : Map[String, NodeSeq] = {
    val result : Map[String, NodeSeq] = new HashMap[String, NodeSeq]()
    destMap foreach ( (t) => result += (t._1 -> t._2))
    result
  }

  //
  //  Clear all data...
  //
  def reset : Unit = {
    destMap.clear()
    sourceMap.clear()
  }
}

class BaseWADLSpec extends FeatureSpec with TransformHandler 
                                       with XPathAssertions {
  //
  // The normalization XSL
  //
  val normXSL = "xsl/normalizeWadl.xsl"

  //
  //  Init xml security lib
  //
  org.apache.xml.security.Init.init()

  private val transformer = transformerFactory.newTransformer(new StreamSource(normXSL))
  private val canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS)
  private val xsd10Asserter = new SchemaAsserter("xsd/XMLSchema1.0.xsd")
  private val wadlAsserter  = new SchemaAsserter("xsd/wadl.xsd")

  def normalizeWADL(in : (String, NodeSeq),
                    format : WADLFormat.Format,
                    xsdVersion : XSDVersion.Version,
                    flattenXSDs : Boolean) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    transformer.clearParameters
    transformer.setParameter("format",format.toString())
    transformer.setParameter("xsdVersion", xsdVersion.toString())
    transformer.setParameter("flattenXsds", flattenXSDs.toString())
    transformer.transform (in, new StreamResult(bytesOut))
    XML.loadString (bytesOut.toString())
  }

  def normalizeWADL(in : NodeSeq,
                    format : WADLFormat.Format = DONT,
                    xsdVersion : XSDVersion.Version = XSD11,
                    flattenXSDs : Boolean = false) : NodeSeq = {
    normalizeWADL(("", in), format, xsdVersion, flattenXSDs)
  }

  //
  //  Asserts that a node sequence is valid XSD 1.0
  //
  def assertXSD10 (in : NodeSeq) {
    xsd10Asserter.assert(in)
  }

  //
  //  Asserts that a node sequence is valid WADL
  //
  def assertWADL (in : NodeSeq) {
    wadlAsserter.assert(in)
  }

  //
  //  Given a node sequence returns a canonicalized XML string that
  //  can be used for comparisons.
  //
  def canon(in : NodeSeq) = {
    new String (canonicalizer.canonicalize(Utility.trim(in(0)).toString().getBytes()))
  }

  //
  // Override scenario so that it resets files
  //
  override protected def scenario(specText: String, testTags: Tag*)(testFun: => Unit) {
    def testCall = {
      testFun
      reset
    }
    super.scenario(specText, testTags:_*)(testCall)
  }
}
