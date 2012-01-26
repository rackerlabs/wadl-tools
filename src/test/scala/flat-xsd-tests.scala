package com.rackspace.cloud.api.wadl.test

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.Converters._

@RunWith(classOf[JUnitRunner])
class FlatXSDSpec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")

  feature ("The WADL normalizer can flaten multiple XSDs covering the same namespace into a single XSD") {

    info("As a developer")
    info("I want to be able to tranfrom multiple XSDs from the same namespace into a single XSD")
    info("So that the XSD can be more easily processed by some WADL/XSD tools.")

    //
    //  The following assertions are common for the next couple of
    //  scenarios
    //
    def commonFlatSingleXSDAssertions : Unit = {
      then("There should be a single XSD produced")
      outputs.size should equal (1)
      and("The name of the XSD file produced should be WADLName-xsd-1.xsd")
      assert (outputs contains "mywadl-xsd-1.xsd")
      and("It's a valid XSD 1.0 file")
      assertXSD10(outputs("mywadl-xsd-1.xsd"))
      and("The resulting schema contains a single string element named test of type xsd:string")
      assert (outputs("mywadl-xsd-1.xsd"), "count(//xsd:element) = 1")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema/xsd:element[@name='test']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema/xsd:element[@type='xsd:string']")
      and("XML Schema attributes should remain in tact")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@elementFormDefault='qualified']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@attributeFormDefault='unqualified']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@targetNamespace='test://schema/a']")
      and("Finally, the QName xsd:string should properly evaluate")
      assert (outputs("mywadl-xsd-1.xsd"), "namespace-uri-from-QName(resolve-QName(/xsd:schema/xsd:element/@type, /xsd:schema/xsd:element)) "+
                                           "= 'http://www.w3.org/2001/XMLSchema'")
    }

    scenario("The WADL contains two schema with the same namespace relatively included") {
      given("a WADL with a schema which includes another schema in the same namespace with a relative path.")
      register ("test://path/to/test/xsd/api.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="other.xsd"/>
                </schema>)
      register ("test://path/to/test/xsd/other.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <element name="test" type="xsd:string"/>
                </schema>)
      val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <grammars>
               <include href="xsd/api.xsd"/>
            </grammars>
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
            </resources>
        </application>)
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      commonFlatSingleXSDAssertions
    }

    scenario("The WADL contains two schema with the same namespace absolute path included") {
      given("a WADL with a schema which includes another schema in the same namespace with an absolute path.")
      register ("test://path/to/test/xsd/api.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="test://a/whole/other/path/other.xsd"/>
                </schema>)
      register ("test://a/whole/other/path/other.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <element name="test" type="xsd:string"/>
                </schema>)
      val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <grammars>
               <include href="xsd/api.xsd"/>
            </grammars>
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
            </resources>
        </application>)
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      commonFlatSingleXSDAssertions
    }

  }
}
