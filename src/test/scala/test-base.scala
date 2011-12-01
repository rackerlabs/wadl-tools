package com.rackspace.cloud.api.wadl.test

import scala.xml._
import javax.xml.transform._
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.apache.xml.security.c14n.Canonicalizer
import org.scalatest.FeatureSpec

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

import WADLFormat._
import XSDVersion._

class BaseWADLSpec extends FeatureSpec {
  //
  //  Init xml security lib
  //
  org.apache.xml.security.Init.init()

  //
  //  Make sure we use Saxon's XSL Transformer
  //
  System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl")

  val normXSL = "xsl/normalizeWadl.xsl"

  private val tfactory = TransformerFactory.newInstance()
  private val transformer = tfactory.newTransformer(new StreamSource(normXSL))
  private val cononicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);

  def normalizeWADL(in : NodeSeq,
                    format : WADLFormat.Format = DONT,
                    xsdVersion : XSDVersion.Version = XSD11,
                    flattenXSDs : Boolean = false) = {
    val bytesOut = new ByteArrayOutputStream()
    transformer.clearParameters
    transformer.setParameter("format",format.toString())
    transformer.setParameter("xsdVersion", xsdVersion.toString())
    transformer.setParameter("flattenXsds", flattenXSDs.toString())
    transformer.transform (new StreamSource(new ByteArrayInputStream(in.toString().getBytes())), new StreamResult(bytesOut))
    XML.loadString (bytesOut.toString())
  }

  //
  //  Given a node sequence returns a cononicalized XML string that
  //  can be used for comparisons.
  //
  def conon(in : NodeSeq) = {
    new String (cononicalizer.canonicalize(Utility.trim(in(0)).toString().getBytes()))
  }
}
