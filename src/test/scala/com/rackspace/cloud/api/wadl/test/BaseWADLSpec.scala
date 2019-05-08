package com.rackspace.cloud.api.wadl.test

import com.rackspace.cloud.api.wadl.WADLNormalizer
import org.apache.xml.security.c14n.Canonicalizer
import org.scalactic.source.Position
import org.scalatest.{FeatureSpec, GivenWhenThen, Tag}

import scala.xml._


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
  override protected def scenario(specText: String, testTags: Tag*)(testFun: => Any)(implicit pos: Position): Unit = {
    def testCall = {
      testFun
      reset
    }
    super.scenario(specText, testTags:_*)(testCall)
  }
}
