package com.rackspace.cloud.api.wadl.test

import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NormalizeXSDSpec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")

  feature ("The WADL normalizer can correctly transform XSD into 1.0 format") {

    info("As a developer")
    info("I want to be able to transform the XSDs in a WADL into 1.0 format")
    info("by correctly processing the schema versioning attributes")
    info("So that I can process the WADL with an XSD 1.0 tool")

    scenario("The WADL does not contain an XSD") {
      Given("a WADL with no schema")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <grammars>
            </grammars>
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
            </resources>
        </application>
      When("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      Then("No additonal documents should be produced")
      outputs.size should equal (0)
    }

    //
    //  The following assertions are common for the next couple of
    //  scenarios
    //
    def commonSingleXSDAssertions : Unit = {
      Then("There should be a single XSD produced")
      outputs.size should equal (1)
      And("The name of the XSD file produced should be WADLName-xsd-1.xsd")
      assert (outputs contains "mywadl-xsd-1.xsd")
      And("It's a valid XSD 1.0 file")
      assertXSD10(outputs("mywadl-xsd-1.xsd"))
      And("The resulting schema contains a single string element named test of type xsd:string")
      assert (outputs("mywadl-xsd-1.xsd"), "count(//xsd:element) = 1")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema/xsd:element[@name='test']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema/xsd:element[@type='xsd:string']")
      And("XML Schema attributes should remain in tact")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@elementFormDefault='qualified']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@attributeFormDefault='unqualified']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@targetNamespace='test://schema/a']")
      And("Finally, the QName xsd:string should properly evaluate")
      assert (outputs("mywadl-xsd-1.xsd"), "namespace-uri-from-QName(resolve-QName(/xsd:schema/xsd:element/@type, /xsd:schema/xsd:element)) "+
                                           "= 'http://www.w3.org/2001/XMLSchema'")
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
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }


    scenario("The WADL points to a single XSD with no versioning schema in a relative forward sub-path") {
      Given("a WADL with a relative path schema")
      register ("test://path/to/test/xsd/schema1.xsd",
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
               <include href="xsd/schema1.xsd"/>
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
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with no versioning schema in a relative backward sub-path") {
      Given("a WADL with a relative path schema")
      register ("test://path/to/xsd/schema1.xsd",
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
               <include href="../../to/xsd/schema1.xsd"/>
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
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD schema in a relative path with flatten set to false") {
      Given("a WADL with a relative path schema")
      register ("test://path/to/test/schema1.xsd",
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
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, false, KEEP)
      //
      //  Assert that the output wadl contains a grammars/inlude element pointing to the schema
      //
      assert (normWADL, "/wadl:application/wadl:grammars/wadl:include[@href = 'test://path/to/test/schema1.xsd']")
    }

    scenario("The WADL points to a single XSD with an element with min version > 1.0") {
      Given("a WADL with an XSD element minVersion > 1.0")
      register ("test://path/to/test/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element name="test" type="xsd:string"/>
                    <element vc:minVersion="1.1" name="test2" type="xsd:string"/>
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
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with an element with min version = 1.0") {
      Given("a WADL with an XSD element minVersion = 1.0")
      register ("test://path/to/test/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
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
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with an element with mix version 1.0/1.1") {
      Given("a WADL with an XSD element mix version 1.0/1.1")
      register ("test://path/to/test/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
                    <element vc:minVersion="1.1" name="test2" type="xsd:string"/>
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
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with no versioning schema in an absolute path") {
      Given("a WADL with an absolute path schema")
      register ("test://path/to/other/schema1.xsd",
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
               <include href="test://path/to/other/schema1.xsd"/>
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
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }
  }
}
