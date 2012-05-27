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
class NormalizeXSD11Spec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")

  feature ("The WADL normalizer can correctly transform XSD into 1.1 format") {

    info("As a developer")
    info("I want to be able to transform the XSDs in a WADL into 1.1 format")
    info("by correctly processing the schema versioning attributes")
    info("So that I can process the WADL with an XSD 1.1 tool")

    scenario("The WADL does not contain an XSD") {
      given("a WADL with no schema")
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
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      then("No additonal documents should be produced")
      outputs.size should equal (0)
    }

    //
    //  The following assertions are common for the next couple of
    //  scenarios
    //
    def commonSingleXSDAssertions : Unit = {
      then("There should be a single XSD produced")
      outputs.size should equal (1)
      and("The name of the XSD file produced should be WADLName-xsd-1.xsd")
      assert (outputs contains "mywadl-xsd-1.xsd")
      and("It's a valid XSD 1.1 file")
      assertXSD11(outputs("mywadl-xsd-1.xsd"))
      and("""The resulting schema contains string elements according to the rules of vc:minVersion, vc:maxVersion selecting those elements who
          are version compatible with XSD 1.1.
          """
          )
      assert (outputs("mywadl-xsd-1.xsd"), "count(//xsd:element) = 2")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema/xsd:element[@name='test']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema/xsd:element[@name='test2']")
      assert (outputs("mywadl-xsd-1.xsd"), "not(/xsd:schema/xsd:element[@name='test3'])")
      assert (outputs("mywadl-xsd-1.xsd"), "not(/xsd:schema/xsd:element[@name='test4'])")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema/xsd:element[@type='xsd:string']")
      and("XML Schema attributes should remain in tact")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@elementFormDefault='qualified']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@attributeFormDefault='unqualified']")
      assert (outputs("mywadl-xsd-1.xsd"), "/xsd:schema[@targetNamespace='test://schema/a']")
      and("Finally, the QName xsd:string should properly evaluate")
      assert (outputs("mywadl-xsd-1.xsd"), "namespace-uri-from-QName(resolve-QName(/xsd:schema/xsd:element[1]/@type, /xsd:schema/xsd:element[1])) "+
                                           "= 'http://www.w3.org/2001/XMLSchema'")
    }

    scenario("The WADL points to a single XSD with no versioning schema in a relative path") {
      given("a WADL with a relative path schema")
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
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with an element with min version = 1.1") {
      given("a WADL with an XSD element minVersion = 1.1")
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
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with an element with min version = 1.1 and max version = 1.1") {
      given("a WADL with an XSD element minVersion = 1.1")
      register ("test://path/to/test/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.1"  name="test" type="xsd:string"/>
                    <element name="test2" type="xsd:string"/>
                    <element vc:minVersion="1.1" vc:maxVersion="1.1" name="test3" type="xsd:string"/>
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
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with an element with min/max versions 1.0/1.1/1.2") {
      given("a WADL with an XSD element min/max versions 1.0/1.1/1.2")
      register ("test://path/to/test/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.2" name="test" type="xsd:string"/>
                    <element vc:minVersion="1.1" name="test2" type="xsd:string"/>
                    <element vc:minVersion="1.1" vc:maxVersion="1.1" name="test3" type="xsd:string"/>
                    <element vc:maxVersion="1.0" name="test4" type="xsd:string"/>
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
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }

    scenario("The WADL points to a single XSD with no versioning schema in an absolute path") {
      given("a WADL with an absolute path schema")
      register ("test://path/to/other/schema1.xsd",
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
      when("the wadl is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      //
      //  Call the common assertions above...
      //
      commonSingleXSDAssertions
    }
  }
}
