package com.rackspace.cloud.api.wadl.test

import java.io.{ByteArrayOutputStream, File}

import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.typesafe.scalalogging.LazyLogging
import javax.xml.transform.stream.{StreamResult, StreamSource}
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class WADLKeepReportSpec extends BaseWADLSpec with LazyLogging {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("svrl","http://purl.oclc.org/dsdl/svrl")
  register ("rax","http://docs.rackspace.com/api")

  val localWADLURI = (new File(System.getProperty("user.dir"),"mywadl.wadl")).toURI.toString

  feature ("The WADL normalizer should keep schematron report if it's told to do so") {

    scenario ("A WADL with no external links should still refernce itself in the report") {
	   Given("a WADL with no external links")
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="GET"/>
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
    }

    scenario ("A WADL with rax:preprocess link should reference the transform") {
	   Given("a WADL with a rax:preprocess link")
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:rax="http://docs.rackspace.com/api">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="POST">
                            <request>
                                <representation mediaType="application/xml">
                                    <rax:preprocess href="xsl/beginStart.xsl"/>
                                </representation>
                            </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
        </application>)
      register("test://path/to/test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:template match="node() | @*">
                 <xsl:copy>
                   <xsl:apply-templates select="@* | node()"/>
                 </xsl:copy>
               </xsl:template>

               <xsl:template match="tst:stepType">
                 <xsl:choose>
                   <xsl:when test=". = 'BEGIN'">
                     <stepType>START</stepType>
                   </xsl:when>
                   <xsl:otherwise>
                     <stepType><xsl:value-of select="."/></stepType>
                   </xsl:otherwise>
                 </xsl:choose>
               </xsl:template>

               </xsl:stylesheet>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart.xsl'")
    }

    scenario ("A WADL with rax:preprocess link should reference the transform (simple transform)") {
	   Given("a WADL with a rax:preprocess link")
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:rax="http://docs.rackspace.com/api">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="POST">
                            <request>
                                <representation mediaType="application/xml">
                                    <rax:preprocess href="xsl/foo.xsl"/>
                                </representation>
                            </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
        </application>)
      register("test://path/to/test/xsl/foo.xsl",
               <foo
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xsl:version="1.0"/>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/foo.xsl'")
    }

    scenario ("A WADL with rax:preprocess link which itself contains a reference to another transform (import)") {
	   Given("a WADL with a rax:preprocess link")
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:rax="http://docs.rackspace.com/api">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="POST">
                            <request>
                                <representation mediaType="application/xml">
                                    <rax:preprocess href="xsl/beginStart.xsl"/>
                                </representation>
                            </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
        </application>)
      register("test://path/to/test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:import href="beginStart2.xsl"/>

               <xsl:template match="node() | @*">
                 <xsl:copy>
                   <xsl:apply-templates select="@* | node()"/>
                 </xsl:copy>
               </xsl:template>

               <xsl:template match="tst:stepType">
                 <xsl:choose>
                   <xsl:when test=". = 'BEGIN'">
                     <stepType>START</stepType>
                   </xsl:when>
                   <xsl:otherwise>
                     <stepType><xsl:value-of select="."/></stepType>
                   </xsl:otherwise>
                 </xsl:choose>
               </xsl:template>

               </xsl:stylesheet>)
      register("test://path/to/test/xsl/beginStart2.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               </xsl:stylesheet>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart.xsl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart2.xsl'")
    }


    scenario ("A WADL with rax:preprocess link which itself contains a reference to another transform (include)") {
	   Given("a WADL with a rax:preprocess link")
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:rax="http://docs.rackspace.com/api">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="POST">
                            <request>
                                <representation mediaType="application/xml">
                                    <rax:preprocess href="xsl/beginStart.xsl"/>
                                </representation>
                            </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
        </application>)
      register("test://path/to/test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:include href="beginStart2.xsl"/>

               <xsl:template match="node() | @*">
                 <xsl:copy>
                   <xsl:apply-templates select="@* | node()"/>
                 </xsl:copy>
               </xsl:template>

               <xsl:template match="tst:stepType">
                 <xsl:choose>
                   <xsl:when test=". = 'BEGIN'">
                     <stepType>START</stepType>
                   </xsl:when>
                   <xsl:otherwise>
                     <stepType><xsl:value-of select="."/></stepType>
                   </xsl:otherwise>
                 </xsl:choose>
               </xsl:template>

               </xsl:stylesheet>)
      register("test://path/to/test/xsl/beginStart2.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               </xsl:stylesheet>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart.xsl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart2.xsl'")
    }

    scenario ("A WADL with rax:preprocess link which itself contains a reference to another transform which includes a schema") {
	   Given("a WADL with a rax:preprocess link")
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:rax="http://docs.rackspace.com/api">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="POST">
                            <request>
                                <representation mediaType="application/xml">
                                    <rax:preprocess href="xsl/beginStart.xsl"/>
                                </representation>
                            </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
        </application>)
      register("test://path/to/test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:include href="beginStart2.xsl"/>

               <xsl:template match="node() | @*">
                 <xsl:copy>
                   <xsl:apply-templates select="@* | node()"/>
                 </xsl:copy>
               </xsl:template>

               <xsl:template match="tst:stepType">
                 <xsl:choose>
                   <xsl:when test=". = 'BEGIN'">
                     <stepType>START</stepType>
                   </xsl:when>
                   <xsl:otherwise>
                     <stepType><xsl:value-of select="."/></stepType>
                   </xsl:otherwise>
                 </xsl:choose>
               </xsl:template>

               </xsl:stylesheet>)
      register("test://path/to/test/xsl/beginStart2.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">
                  <xsl:import-schema schemaLocation="../xsd/mytest-other.xsd"/>
               </xsl:stylesheet>)
      register ("test://path/to/test/xsd/mytest-other.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a/other">
                    <element name="other" type="xsd:string"/>
                </schema>
      )
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart.xsl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart2.xsl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest-other.xsd'")
    }


    scenario ("A WADL with rax:preprocess which contains an embeded transform  a reference to another transform which includes a schema") {
	   Given("a WADL with a rax:preprocess link")
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:rax="http://docs.rackspace.com/api">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="POST">
                            <request>
                                <representation mediaType="application/xml">
                                    <rax:preprocess>
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:include href="xsl/beginStart2.xsl"/>

               <xsl:template match="node() | @*">
                 <xsl:copy>
                   <xsl:apply-templates select="@* | node()"/>
                 </xsl:copy>
               </xsl:template>

               <xsl:template match="tst:stepType">
                 <xsl:choose>
                   <xsl:when test=". = 'BEGIN'">
                     <stepType>START</stepType>
                   </xsl:when>
                   <xsl:otherwise>
                     <stepType><xsl:value-of select="."/></stepType>
                   </xsl:otherwise>
                 </xsl:choose>
               </xsl:template>

               </xsl:stylesheet>
                                    </rax:preprocess>
                                </representation>
                            </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
        </application>)
      register("test://path/to/test/xsl/beginStart2.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">
                  <xsl:import-schema schemaLocation="../xsd/mytest-other.xsd"/>
               </xsl:stylesheet>)
      register ("test://path/to/test/xsd/mytest-other.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a/other">
                    <element name="other" type="xsd:string"/>
                </schema>
      )
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsl/beginStart2.xsl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest-other.xsd'")
    }


    scenario ("A WADL with an external link should have the link reported") {
	   Given("a WADL with an external link")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                <method name="GET"/>
             </resource_type>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo">
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/other.wadl'")
    }

    scenario ("A WADL with an external link which itself has an external link be reported") {
	   Given("a WADL with an external link")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                <method name="GET"/>
                <resource path="d" type="other2.wadl#bar"/>
             </resource_type>
        </application>
      )
      register ("test://path/to/test/other2.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="bar">
                <method name="GET"/>
             </resource_type>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo">
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/other.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/other2.wadl'")
    }

    scenario ("A WADL with an external link and an external XSD should have both links reported") {
	   Given("a WADL with an external link")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                <method name="GET"/>
             </resource_type>
        </application>
      )
      register ("test://path/to/test/xsd/mytest.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <element name="test" type="xsd:string"/>
                </schema>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                <include href="xsd/mytest.xsd"/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo">
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/other.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest.xsd'")
    }

    scenario ("A WADL with an external JSONSchema should have its links reported") {
	   Given("a WADL with an external JSON Schema")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                <include href="src/test/test-samples/test-schema.json"/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="src/test/test-samples/resource_type.wadl.xml#foo"/>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, s"/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = '$localWADLURI'")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                        satisfies contains($d,'src/test/test-samples/resource_type.wadl.xml')""")
      assert(normWADL, """some $u in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='unparsedReference']/svrl:text
                        satisfies contains($u, 'src/test/test-samples/test-schema.json')""")
    }

    scenario ("A WADL which refers to an external entity should have the entity reported") {
      Given("a WADL with an external entity")
      val wadlOutBytes = new ByteArrayOutputStream()
      When("the WADL is normalized")
      wadl.normalize(new StreamSource(new File("src/test/test-samples/entity.wadl.xml")), new StreamResult(wadlOutBytes), TREE, XSD11, true, KEEP, true)
      val normWADL = XML.loadString (wadlOutBytes.toString())
      Then ("The normalize wadl should contain a report with the correct documents referenced")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                          satisfies contains($d,'src/test/test-samples/entity.wadl.xml')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/common.ent')""")
    }

    scenario ("A WADL which refers to an external entity and samples should have all of these dependecies reported") {
      Given("a WADL with an external entity and entity samples")
      val wadlOutBytes = new ByteArrayOutputStream()
      When("the WADL is normalized")
      wadl.normalize(new StreamSource(new File("src/test/test-samples/entity-withsamples.wadl.xml")), new StreamResult(wadlOutBytes), TREE, XSD11, true, KEEP, true)
      val normWADL = XML.loadString (wadlOutBytes.toString())
      Then ("The normalize wadl should contain a report with the correct documents referenced")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                          satisfies contains($d,'src/test/test-samples/entity-withsamples.wadl.xml')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/common.ent')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/content-samples/metadata_item.xml')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/content-samples/metadata_item.json')""")
    }

    scenario("A WADL which refers to an external entity in another file should have both links reported") {
      Given("a WADL with an external entity in a separate file")
      val wadlFile = new File ("src/test/test-samples/entity2.wadl.xml")
      val inWADL = (wadlFile.toURI.toString,
                    XML.loadFile(wadlFile))
      When ("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then ("The normalized wadl should contain a report with the correct documents referenced")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                          satisfies contains($d,'src/test/test-samples/entity2.wadl.xml')""")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                          satisfies contains($d,'src/test/test-samples/entity2-method.wadl.xml')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/common.ent')""")
    }

    scenario("A WADL which refers to an external entity in an xincluded file should have both links reported") {
      Given("a WADL with an external entity in a separate file")
      val wadlFile = new File ("src/test/test-samples/xinclude-entity.wadl.xml")
      val inWADL = (wadlFile.toURI.toString,
                    XML.loadFile(wadlFile))
      When ("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then ("The normalized wadl should contain a report with the correct documents referenced")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                          satisfies contains($d,'src/test/test-samples/xinclude-entity.wadl.xml')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/common.ent')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/xinclude-method.wadl.xml')""")
    }

    scenario("A WADL which refers to an external external WADL whch contains an entity in an xincluded file should have all links reported") {
      Given("a WADL with an external entity in a separate file")
      val wadlFile = new File ("src/test/test-samples/xinclude-entity2.wadl.xml")
      val inWADL = (wadlFile.toURI.toString,
                    XML.loadFile(wadlFile))
      When ("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then ("The normalized wadl should contain a report with the correct documents referenced")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                          satisfies contains($d,'src/test/test-samples/xinclude-entity2.wadl.xml')""")
      assert(normWADL, """some $d in /wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document
                          satisfies contains($d,'src/test/test-samples/xinclude-method2.wadl.xml')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/common.ent')""")
      assert(normWADL, """some $e in /wadl:application/svrl:schematron-output/svrl:successful-report[@role='includeReference']/svrl:text
                          satisfies contains($e,'src/test/test-samples/xinclude-method.wadl.xml')""")
    }

    scenario ("A WADL with an external link and an multiple external XSD should have both links reported") {
	   Given("a WADL with an external link")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                <method name="GET"/>
             </resource_type>
        </application>
      )
      register ("test://path/to/test/xsd/mytest.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="mytest2.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
      )
      register ("test://path/to/test/xsd/mytest2.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <import namespace="test://schema/a/other" schemaLocation="mytest-other.xsd"/>
                    <element name="test2" type="xsd:string"/>
                </schema>
      )
      register ("test://path/to/test/xsd/mytest-other.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a/other">
                    <element name="other" type="xsd:string"/>
                </schema>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                <include href="xsd/mytest.xsd"/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo">
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/other.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest.xsd'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest2.xsd'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest-other.xsd'")
    }

    scenario ("A WADL with an external link and an multiple external XSD referenced from interal XSD should have all links reported") {
	   Given("a WADL with an external link")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                <method name="GET"/>
             </resource_type>
        </application>
      )
      register ("test://path/to/test/xsd/mytest2.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <import namespace="test://schema/a/other" schemaLocation="mytest-other.xsd"/>
                    <element name="test2" type="xsd:string"/>
                </schema>
      )
      register ("test://path/to/test/xsd/mytest-other.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a/other">
                    <element name="other" type="xsd:string"/>
                </schema>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                 <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="xsd/mytest2.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo">
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP, true)
      Then("The normalized wadl should contain a report with the correct document referenced")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/mywadl.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/other.wadl'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest2.xsd'")
      assert(normWADL, "/wadl:application/svrl:schematron-output/svrl:active-pattern[@name='References']/@document = 'test://path/to/test/xsd/mytest-other.xsd'")
    }
  }
}
