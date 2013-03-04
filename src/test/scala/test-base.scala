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
import java.net.URL
import org.apache.xml.security.c14n.Canonicalizer
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Tag
import org.scalatest.TestFailedException
import org.xml.sax.SAXException
import net.sf.saxon.lib.OutputURIResolver
import net.sf.saxon.lib.NamespaceConstant

import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.WADLNormalizer

class SchemaAsserter(xsdSource : URL, useSaxon : Boolean = false) {
  private val factory = {
    if (useSaxon) {
      val inst = Class.forName("com.saxonica.jaxp.SchemaFactoryImpl").newInstance.asInstanceOf[SchemaFactory]
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
      case unknown => throw new TestFailedException ("Unkown XPath assert error! "+node, unknown, 4)
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
    def resolve(href : String, base : String) = {
      val source = sourceMap getOrElse (href, defaultResolver.resolve(href, base))
      if (source.isInstanceOf[StreamSource]) {
        source.asInstanceOf[StreamSource].getInputStream().reset()
      }
      source
    }
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
    val conv : (String, ByteArrayInputStream) = (url, xml);
    val streamSource : StreamSource = new StreamSource (conv._2)
    streamSource.setSystemId(conv._1)
    sourceMap += (url -> streamSource)
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
                                       with XPathAssertions
                                       with GivenWhenThen {

  val wadl = new WADLNormalizer(transformerFactory)

  //
  //  Init xml security lib
  //
  org.apache.xml.security.Init.init()

  private val canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS)
  private val xsd10Asserter = new SchemaAsserter(getClass().getClassLoader().getResource("XMLSchema1.0.xsd"))
  private val xsd11Asserter = new SchemaAsserter(getClass().getClassLoader().getResource("XMLSchema1.1.xsd"))
  private val wadlAsserter  = new SchemaAsserter(getClass().getClassLoader().getResource("wadl.xsd"))

  //
  //  Asserts that a node sequence is valid XSD 1.0
  //
  def assertXSD10 (in : NodeSeq) {
    xsd10Asserter.assert(in)
  }

  //
  //  Asserts that a node sequence is valid XSD 1.0
  //
  def assertXSD11 (in : NodeSeq) {
    xsd11Asserter.assert(in)
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
