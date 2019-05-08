package com.rackspace.cloud.api.wadl.test

import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatestplus.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class CopyXSDSpec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")

  feature ("The WADL normalizer can correctly transform XSD into 1.1 format") {

    info("As a developer")
    info("I want to be able to normalize a WADL without affecting XSDs")

    def commonXSDAssertions(normWADL : NodeSeq)  : Unit = {
      Then("There should no additional files produced")
      outputs.size should equal (0)
      And("The grammar file should be copied with an absolute path for the XSD")
      assert (normWADL, "/wadl:application/wadl:grammars/wadl:include[@href='test://path/to/test/schema1.xsd']")
    }

    scenario("The WADL points to a single XSD with no versioning schema in a relative path") {
      Given("a WADL with a relative path schema")
      register ("test://path/to/test/schema1.xsd",
                  <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <element name="test" type="xsd:string"/>
                    <element name="test2" type="xsd:string"/>
                </schema>)
        val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <grammars>
               <include href="schema1.xsd"/>
            </grammars>
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
            </resources>
        </application>)
          When("the wadl is normalized")
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
        commonXSDAssertions(normWADL)
      }

    scenario("The WADL points to a single XSD with no versioning schema in an absolute path") {
      Given("a WADL with a relative path schema")
      register ("test://path/to/test/schema1.xsd",
                  <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <element name="test" type="xsd:string"/>
                    <element name="test2" type="xsd:string"/>
                </schema>)
        val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <grammars>
               <include href="test://path/to/test/schema1.xsd"/>
            </grammars>
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
            </resources>
        </application>)
          When("the wadl is normalized")
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
        commonXSDAssertions(normWADL)
      }

    scenario("The WADL points to a single XSD with no versioning schema embeded") {
      Given("a WADL with an embeded schema")
        val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <grammars>
                  <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <element name="test" type="xsd:string"/>
                    <element name="test2" type="xsd:string"/>
                </schema>
            </grammars>
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
            </resources>
        </application>)
          When("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      assert (normWADL, "count(//xsd:element) = 2")
      assert (normWADL, "/wadl:application/wadl:grammars/xsd:schema/xsd:element[@name='test']")
      assert (normWADL, "/wadl:application/wadl:grammars/xsd:schema/xsd:element[@name='test2']")
      assert (normWADL, "/wadl:application/wadl:grammars/xsd:schema/xsd:element[@type='xsd:string']")
      And("XML Schema attributes should remain in tact")
      assert (normWADL, "/wadl:application/wadl:grammars/xsd:schema[@elementFormDefault='qualified']")
      assert (normWADL, "/wadl:application/wadl:grammars/xsd:schema[@attributeFormDefault='unqualified']")
      assert (normWADL, "/wadl:application/wadl:grammars/xsd:schema[@targetNamespace='test://schema/a']")
      And("Finally, the QName xsd:string should properly evaluate")
      assert (normWADL, "namespace-uri-from-QName(resolve-QName(/wadl:application/wadl:grammars/xsd:schema/xsd:element[1]/@type, "+
              "/wadl:application/wadl:grammars/xsd:schema/xsd:element[1])) "+
              "= 'http://www.w3.org/2001/XMLSchema'")
      assert (normWADL, "namespace-uri-from-QName(resolve-QName(/wadl:application/wadl:grammars/xsd:schema/xsd:element[2]/@type, "+
              "/wadl:application/wadl:grammars/xsd:schema/xsd:element[2])) "+
              "= 'http://www.w3.org/2001/XMLSchema'")
    }
  }
}
