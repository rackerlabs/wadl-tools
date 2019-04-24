package com.rackspace.cloud.api.wadl.test

import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner

import scala.xml._

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
    def commonSchemaAssertions(schema : NodeSeq, namespace : String ="test://schema/a") : Unit = {
      And("Should be a valid 1.0 schema")
      assertXSD10(schema)
      And("The resulting schema contains a single string element named test of type xsd:string")
      assert (schema, "count(//xsd:element) = 1")
      assert (schema, "/xsd:schema/xsd:element[@name='test']")
      assert (schema, "/xsd:schema/xsd:element[@type='xsd:string']")
      And("XML Schema attributes should remain in tact")
      assert (schema, "/xsd:schema[@elementFormDefault='qualified']")
      assert (schema, "/xsd:schema[@attributeFormDefault='unqualified']")
      assert (schema, "/xsd:schema[@targetNamespace='"+namespace+"']")
      And("Finally, the QName xsd:string should properly evaluate")
      assert (schema, "namespace-uri-from-QName(resolve-QName(/xsd:schema/xsd:element/@type, /xsd:schema/xsd:element)) "+
                                      "= 'http://www.w3.org/2001/XMLSchema'")
    }

    def commonFlatSingleXSDAssertions : Unit = {
      Then("There should be a single XSD produced")
      outputs.size should equal (1)
      And("The name of the XSD file produced should be WADLName-xsd-1.xsd")
      assert (outputs contains "mywadl-xsd-1.xsd")
      And("It's a valid XSD 1.0 file")
      commonSchemaAssertions(outputs("mywadl-xsd-1.xsd"))
    }

    scenario("The WADL contains two schema with the same namespace relatively included") {
      Given("a WADL with a schema which includes another schema in the same namespace with a relative path.")
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
      When("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      commonFlatSingleXSDAssertions
    }

    scenario("The WADL contains two schema with the same namespace absolute path included") {
      Given("a WADL with a schema which includes another schema in the same namespace with an absolute path.")
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
      When("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      commonFlatSingleXSDAssertions
    }


    //
    //  The following assertions are common for the next couple of
    //  scenarios
    //
    def commonFlatImportXSDAssertions : Unit = {
      Then("There should be two XSD produced")
      outputs.size should equal (2)
      And("The name of the first XSD file produced should be WADLName-xsd-1.xsd")
      assert (outputs contains "mywadl-xsd-1.xsd")
      And("The name of the second XSD file produced should be WADLName-xsd-2.xsd")
      assert (outputs contains "mywadl-xsd-2.xsd")
      And("They should be valid XSD 1.0 files")
      commonSchemaAssertions(outputs("mywadl-xsd-1.xsd"))
      commonSchemaAssertions(outputs("mywadl-xsd-2.xsd"), "test://schema/b")
    }

    scenario("The WADL contains a schema that imports another schema in a relative path") {
      Given("a WADL wthi a schema which imports another schema with a relative path")
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
                    <import namespace="test://schema/b" schemaLocation="another.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsd/another.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/b">
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
      When("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      commonFlatImportXSDAssertions
    }

    scenario("The WADL contains a schema that imports another schema in a absolute path") {
      Given("a WADL wthi a schema which imports another schema with a absolute path")
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
                    <import namespace="test://schema/b" schemaLocation="test://path/to/test/xsd/another.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsd/another.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/b">
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
      When("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      commonFlatImportXSDAssertions
    }

  }
}
