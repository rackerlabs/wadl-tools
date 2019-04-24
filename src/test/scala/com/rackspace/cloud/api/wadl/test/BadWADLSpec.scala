package com.rackspace.cloud.api.wadl.test

import java.io.ByteArrayOutputStream

import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.typesafe.scalalogging.LazyLogging
import javax.xml.transform.stream.{StreamResult, StreamSource}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BadWADLSpec extends BaseWADLSpec with LazyLogging {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")

  feature ("The WADL normalizer should check references before processing and fail if a references is invalid.") {
    scenario ("A WADL with a link not containing a #, should be reqected") {
	   Given("a WADL with a link missing a #")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words foo and #")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("#"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing link should be rejected") {
	   Given("a WADL with a missing link")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing external link should be rejected (missing file)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="other.wadl#foo"/>
                     </resource>
                 </resource>
             </resources>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'other.wadl#foo' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("other.wadl#foo"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing external link should be rejected rax:preprocess") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart.xsl' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("beginStart.xsl"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with valid external link which contains a missing external link should be rejected rax:preprocess") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
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
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart2.xsl' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("beginStart2.xsl"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsl/beginStart.xsl"))
    }

    scenario ("A WADL with valid external link which contains an invalid XSLT should be rejected rax:preprocess") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
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
      register("test://test/xsl/beginStart2.xsl", <foo />)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart2.xsl' and 'does not appear to be a valid XSLT.'.")
      assert(thrown.getMessage().contains("beginStart2.xsl"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSLT."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsl/beginStart.xsl"))
    }

    scenario ("A WADL with valid external link which contains a missing external link should be rejected rax:preprocess (import)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
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
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart2.xsl' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("beginStart2.xsl"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsl/beginStart.xsl"))
    }

    scenario ("A WADL with valid external link which contains an invalid XSLT should be rejected rax:preprocess (import)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
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
      register("test://test/xsl/beginStart2.xsl", <foo />)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart2.xsl' and 'does not appear to be a valid XSLT.'.")
      assert(thrown.getMessage().contains("beginStart2.xsl"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSLT."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsl/beginStart.xsl"))
    }

    scenario ("A WADL with valid external link which contains a missing external link should be rejected rax:preprocess (schema)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:import-schema schemaLocation="../xsd/my.xsd"/>

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
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'my.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("my.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsl/beginStart.xsl"))
    }


    scenario ("A WADL with valid external link which contains an invalid XSD should be rejected rax:preprocess (schema)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:import-schema schemaLocation="../xsd/my.xsd"/>

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
      register("test://test/xsd/my.xsd", <foo />)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'my.xsd' and 'does not appear to be a valid XSD schema.'.")
      assert(thrown.getMessage().contains("my.xsd"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSD schema."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsl/beginStart.xsl"))
    }


    scenario ("A WADL with valid external link which contains an valid XSD which itself contains a missing link should be rejected rax:preprocess (schema)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:import-schema schemaLocation="../xsd/my.xsd"/>

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
      register("test://test/xsd/my.xsd",
             <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a/other">
                    <include schemaLocation="foo.xsd"/>
                </schema>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("foo.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsd/my.xsd"))
    }

    scenario ("A WADL with valid external link which contains an valid XSD which references a bad schema should be rejected rax:preprocess (schema)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register("test://test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:import-schema schemaLocation="../xsd/my.xsd"/>

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
      register("test://test/xsd/my.xsd",
             <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a/other">
                    <include schemaLocation="foo.xsd"/>
                </schema>)
      register("test://test/xsd/foo.xsd", <foo />)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo.xsd' and 'does not appear to be a valid XSD schema.'.")
      assert(thrown.getMessage().contains("foo.xsd"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSD schema."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/xsd/my.xsd"))
    }


    scenario ("A WADL with a missing external link should be rejected rax:preprocess, embedded XSLT (include)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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

                                            <xsl:include href="xsl/beginStart.xsl"/>

                                        </xsl:stylesheet>
                                    </rax:preprocess>
                                </representation>
                            </request>
                       </method>
                     </resource>
                 </resource>
             </resources>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart.xsl' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("beginStart.xsl"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing external link should be rejected rax:preprocess, embedded XSLT (import)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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

                                            <xsl:import href="xsl/beginStart.xsl"/>

                                        </xsl:stylesheet>
                                    </rax:preprocess>
                                </representation>
                            </request>
                       </method>
                     </resource>
                 </resource>
             </resources>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart.xsl' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("beginStart.xsl"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with link of the wrong type should be rejected (rax:preprocess)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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
        </application>
      register ("test://test/xsl/beginStart.xsl", <foo />)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart.xsl' and 'does not appear to be a valid XSLT.'.")
      assert(thrown.getMessage().contains("beginStart.xsl"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSLT."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a link of the wrong type should be rejected rax:preprocess, embedded XSLT (import)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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

                                            <xsl:import href="xsl/beginStart.xsl"/>

                                        </xsl:stylesheet>
                                    </rax:preprocess>
                                </representation>
                            </request>
                       </method>
                     </resource>
                 </resource>
             </resources>
        </application>
      register ("test://test/xsl/beginStart.xsl", <foo />)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart.xsl' and 'does not appear to be a valid XSLT.'.")
      assert(thrown.getMessage().contains("beginStart.xsl"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSLT."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a link of the wrong type should be rejected rax:preprocess, embedded XSLT (include)") {
	   Given("a WADL with a link missing external link")
	   val inWADL =
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

                                            <xsl:include href="xsl/beginStart.xsl"/>

                                        </xsl:stylesheet>
                                    </rax:preprocess>
                                </representation>
                            </request>
                       </method>
                     </resource>
                 </resource>
             </resources>
        </application>
      register ("test://test/xsl/beginStart.xsl", <foo />)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'beginStart.xsl' and 'does not appear to be a valid XSLT.'.")
      assert(thrown.getMessage().contains("beginStart.xsl"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSLT."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }


    scenario ("A WADL with a missing external link should be rejected") {
	   Given("a WADL with a link missing external link")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <method id="bar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="other.wadl#foo"/>
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'other.wadl#foo' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("other.wadl#foo"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with an external link of the wrong type should be rejected") {
	   Given("a WADL with an external link of the wrong type")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="other.wadl#foo"/>
                     </resource>
                 </resource>
             </resources>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'other.wadl#foo' and 'method'")
      assert(thrown.getMessage().contains("other.wadl#foo"))
      assert(thrown.getMessage().contains("method"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing include should be rejected") {
	   Given("a WADL with a missing include")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
               <include href="schema.xsd"/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'schema.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("schema.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing code sample should be rejected") {
	   Given("a WADL with a missing code sample")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
               <request>
                  <representation>
                    <doc xml:lang="EN">
                        <xsdxt:code href="missing_sample.xml"/>
                    </doc>
                  </representation>
               </request>
             </method>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'missing_sample.xml' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("missing_sample.xml"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    import java.io.File
    val localDir = new File(System.getProperty("user.dir"))
    val localWADLURI = (new File(localDir,"mywadl.wadl")).toURI.toString
    val sampleXMLFilePath = (new File(localDir, "src/test/test-samples/hello.xml")).toURI.toString
    val sampleJSONFilePath = (new File(localDir, "src/test/test-samples/hello.json")).toURI.toString

    logger.debug ("TEST: "+localWADLURI)
    scenario ("A WADL with a valid code sample should be accepted") {
	   Given("a WADL with a missing code sample")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
               <request>
                  <representation>
                    <doc xml:lang="EN">
                        <xsdxt:code href={sampleJSONFilePath}/>
                    </doc>
                  </representation>
               </request>
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a valid code sample should be accepted (relative)") {
	   Given("a WADL with a missing code sample")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
               <request>
                  <representation>
                    <doc xml:lang="EN">
                        <xsdxt:code href="src/test/test-samples/hello.json"/>
                    </doc>
                  </representation>
               </request>
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a valid code sample should be accepted (xml)") {
	   Given("a WADL with a missing code sample")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
               <request>
                  <representation>
                    <doc xml:lang="EN">
                        <xsdxt:code href={sampleXMLFilePath}/>
                    </doc>
                  </representation>
               </request>
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a valid code sample should be accepted (xml, relative)") {
	   Given("a WADL with a missing code sample")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
               <request>
                  <representation>
                    <doc xml:lang="EN">
                        <xsdxt:code href="src/test/test-samples/hello.xml"/>
                    </doc>
                  </representation>
               </request>
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a non-schema include should be accepted") {
	   Given("a WADL with a non-schema include")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                    <include href={sampleJSONFilePath}/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a non-schema include should be accepted (relative)") {
	   Given("a WADL with a non-schema include")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                    <include href="src/test/test-samples/hello.json"/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a non-schema include should be accepted (xml)") {
	   Given("A WADL with a non-schema include")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                    <include href={sampleXMLFilePath}/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a non-schema include should be accepted (xml, relative)") {
	   Given("A WADL with a non-schema include")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                    <include href="src/test/test-samples/hello.xml"/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a non-schema include should be accepted (multiple XML, JSON)") {
	   Given("a WADL with a non-schema include")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                    <include href={sampleJSONFilePath}/>
                    <include href={sampleXMLFilePath}/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with multiple schema and  non-schema includes, should follow schema includes and report errors") {
	   Given("a WADL with multiple schema and non-schema includes")
      val badSchema=register ("test://path/to/bad/xsd/bad.xsd",  <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="b.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>)
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                    <include href={sampleJSONFilePath}/>
                    <include href={sampleXMLFilePath}/>
                    <include href="test://path/to/bad/xsd/bad.xsd"/>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
             </method>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/bad/xsd/bad.xsd"))
    }

    scenario ("A WADL with a schema include/import should be accepted") {
	   Given("a WADL with multiple schema include/import")
	   val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsdxt="http://docs.rackspacecloud.com/xsd-ext/v1.0"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                   <xsd:schema targetNamespace="http://wadl.dev.java.net/2009/02">
                      <xsd:include schemaLocation="xsd/wadl.xsd"/>
                      <xsd:import namespace="http://www.w3.org/2001/XMLSchema" schemaLocation="xsd/XMLSchema1.1.xsd"/>
                   </xsd:schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo">
             </method>
        </application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
    }

    scenario ("A WADL with a parameter reference, that does not point to a parameter should be rejected") {
	   Given("a WADL with a parameter reference pointing to a method")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <param href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo' and 'param'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("param"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a representation reference, that does not point to a representation should be rejected") {
	   Given("a WADL with a representation reference pointing to a method")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                       <method name="POST">
	                      <request>
                            <representation href="#foo"/>
                          </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo' and 'representation'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("representation"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a method reference, that does not point to a method should be rejected") {
	   Given("a WADL with a method reference pointing to a representation")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                         <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <representation id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo' and 'method'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("method"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource type reference, that does not point to a resource_type should be rejected") {
	   Given("a WADL with a resource type  reference pointing to a representation")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="GET">
                            <response status="200">
                                <representation mediaType="application/xml">
                                    <param name="location" style="plain" type="xsd:anyURI">
                                       <link resource_type="#foo"/>
                                    </param>
                                </representation>
                            </response>
                        </method>
                     </resource>
                 </resource>
             </resources>
             <representation id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo' and 'resource_type'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("resource_type"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of references some of which are missing '#' should be rejected") {
	   Given("a WADL with a resource with a set of  references some of which are missing '#'")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="foo #bar jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'foo jar' and 'missing '#''")
      assert(thrown.getMessage().contains("foo jar"))
      assert(thrown.getMessage().contains("missing '#'"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of references some of which are missing should be rejected") {
	   Given("a WADL with a resource with a set of  references some of which are missing")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="#foo #bar #not #jar #here">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words '#not #here' and 'do not seem to exist in this wadl'")
      assert(thrown.getMessage().contains("#not #here"))
      assert(thrown.getMessage().contains("do not seem to exist in this wadl"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }


    scenario ("A WADL with a resource with a set of external references some of which are missing should be rejected") {
	   Given("a WADL with a resource with a set of external references some of which are missing")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo other.wadl#bar other.wadl#not other.wadl#jar other.wadl#here">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'other.wadl#not other.wadl#here' and 'do not seem to exist'")
      assert(thrown.getMessage().contains("other.wadl#not other.wadl#here"))
      assert(thrown.getMessage().contains("do not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of references some of which are pointing to the wrong type be rejected") {
	   Given("a WADL with a resource with a set of  references some of which are pointing to the wrong type")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="#foo #bar #jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <method id="jar"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words '#jar' and 'are not pointing to a resource type.'")
      assert(thrown.getMessage().contains("'#jar'"))
      assert(thrown.getMessage().contains("are not pointing to a resource type."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references some of which are pointing to the wrong type should be rejected") {
	   Given("a WADL with a resource with a set of external references some of which are pointing to the wrong type")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <method id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo other.wadl#bar other.wadl#jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'other.wadl#jar' and 'are not pointing to a resource type.'")
      assert(thrown.getMessage().contains("'other.wadl#jar'"))
      assert(thrown.getMessage().contains("are not pointing to a resource type."))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references some of which are missing, should be rejected (missing file)") {
	   Given("a WADL with a resource with a set of external references some of which are missing")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo other.wadl#bar another.wadl#not other.wadl#jar another.wadl#here">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'another.wadl#not another.wadl#here' and 'do not seem to exist'")
      assert(thrown.getMessage().contains("another.wadl#not another.wadl#here"))
      assert(thrown.getMessage().contains("do not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing resources") {
	   Given("a WADL with a resource with a set of external references, the external wadl itself contains missing resources")
      register ("test://path/to/test/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                 <resource path="bar">
                     <method href="#myMethod"/>
                 </resource>
             </resource_type>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="other.wadl#foo other.wadl#bar other.wadl#jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'myMethod' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("myMethod"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/other.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources") {
	   Given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
      register ("test://path/another.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <method id="myMethod2"/>
        </application>
      )
      register ("test://path/to/test/foo/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                 <resource path="bar">
                     <method href="../../../another.wadl#myMethod"/>
                 </resource>
             </resource_type>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="foo/other.wadl#foo foo/other.wadl#bar foo/other.wadl#jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'myMethod' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("myMethod"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/foo/other.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources (missing file)") {
	   Given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
      register ("test://path/to/test/foo/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                 <resource path="bar">
                     <method href="../../../another.wadl#myMethod"/>
                 </resource>
             </resource_type>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="foo/other.wadl#foo foo/other.wadl#bar foo/other.wadl#jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'myMethod' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("myMethod"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/foo/other.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources (two WADLs down)") {
	   Given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
      register ("test://path/yet/another/wadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <method id="doGet2" name="GET"/>
        </application>
      )
      register ("test://path/another.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <resource_type id="barType">
                <method href="yet/another/wadl.wadl#doGet"/>
           </resource_type>
        </application>
      )
      register ("test://path/to/test/foo/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                 <resource path="bar" type="../../../another.wadl#barType"/>
             </resource_type>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="foo/other.wadl#foo foo/other.wadl#bar foo/other.wadl#jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'doGet' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("doGet"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/another.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources (two WADLs down, missing file)") {
	   Given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
      register ("test://path/another.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <resource_type id="barType">
                <method href="yet/another/wadl.wadl#doGet"/>
           </resource_type>
        </application>
      )
      register ("test://path/to/test/foo/other.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resource_type id="foo">
                 <resource path="bar" type="../../../another.wadl#barType"/>
             </resource_type>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>
      )
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c" type="foo/other.wadl#foo foo/other.wadl#bar foo/other.wadl#jar">
                     </resource>
                 </resource>
             </resources>
             <resource_type id="foo"/>
             <resource_type id="bar"/>
             <resource_type id="jar"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'doGet' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("doGet"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/another.wadl"))
    }

    scenario ("A WADL with an embeded schema, with a missing reference, should be rejected (import)") {
	   Given("a WADL with an embeded schema schema with a missing reference")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <import namespace="test://schema/b" schemaLocation="b.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
	                 <method name="GET"/>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with an embeded schema, with a missing reference, should be rejected (include)") {
	   Given("a WADL with an embeded schema schema with a missing reference")
	   val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="b.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
	                 <method name="GET"/>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      Then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with an include that does not point to a schema should be rejected (embeded)") {
	   Given("a WADL with an include that does not point to a schema")
      register ("test://path/to/test/xsd/schema1.xsd", <junk />)
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="xsd/schema1.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
	                    <method href="#foo"/>
                     </resource>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }

      Then("An exception should be thrown with the words 'schema1.xsd' and 'does not appear to be a valid XSD schema'.")
      assert(thrown.getMessage().contains("schema1.xsd"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSD schema"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with an embeded schema, which contains a schema that has a missing reference, should be rejected") {
	   Given("a WADL with an embeded schema, the linked schema is missing a reference")
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="b.xsd"/>
                    <element name="test" type="xsd:string"/>
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
                    <include schemaLocation="xsd/schema1.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
	                 <method name="GET"/>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/xsd/schema1.xsd"))
    }

    scenario ("A WADL with an embeded schema, which contains a schema that has a missing reference, should be rejected (two levels)") {
	   Given("a WADL with an embeded schema, the linked schema is missing a reference")
      register ("test://path/to/test/xsd/b.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="c.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
              )
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="b.xsd"/>
                    <element name="test" type="xsd:string"/>
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
                    <include schemaLocation="xsd/schema1.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
	                 <method name="GET"/>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'c.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("c.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/xsd/b.xsd"))
    }

    scenario ("A WADL with an embeded schema, which contains a schema that is referencing a non-schema, should be rejected") {
	   Given("a WADL with an embeded schema, that contains a schema this is referencing a non-schema")
      register ("test://path/to/test/xsd/b.xsd", <junk />)
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                    <include schemaLocation="b.xsd"/>
                    <element name="test" type="xsd:string"/>
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
                    <include schemaLocation="xsd/schema1.xsd"/>
                    <element name="test" type="xsd:string"/>
                </schema>
             </grammars>
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
	                 <method name="GET"/>
                 </resource>
             </resources>
             <method id="foo"/>
        </application>)
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'b.xsd' and 'does not appear to be a valid XSD schema'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSD schema"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/xsd/schema1.xsd"))
    }

    scenario ("The WADL wihch points to an XSD which is missing an import") {
      Given("a WADL that contains an XSD which is missing an import")
        val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
        register("test://app/xsd/simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <import namespace="http://www.w3.org/1999/XSL/Transform"
                           schemaLocation="transform.xsd"/>
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      When("the wadl is translated")
      val thrown = intercept[Exception] {
        wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      Then("An exception should be thrown with the words 'transform.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("transform.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
        assert(thrown.getMessage().contains("test://app/xsd/simple.xsd"))
    }

    scenario ("A WADL which refers to a missing external entity should have an error reported") {
      Given("a WADL with a missing external entity")
      val wadlOutBytes = new ByteArrayOutputStream()
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        wadl.normalize(new StreamSource(new File("src/test/bad-test-samples/missing-entity.wadl.xml")), new StreamResult(wadlOutBytes), TREE, XSD11, true, KEEP, true)
      }
      Then ("An exception should be thrown with the words 'missing-entity.wadl.xml' and 'missing.ent'")
      assert (thrown.getMessage().contains("missing-entity.wadl.xml"))
      assert (thrown.getMessage().contains("missing.ent"))
    }

    scenario ("A WADL which xincludes a missing file should fail with a correct error reported") {
      Given("a WADL with a missing xinclude")
      val wadlOutBytes = new ByteArrayOutputStream()
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        wadl.normalize(new StreamSource(new File("src/test/bad-test-samples/missing-xinclude.wadl.xml")), new StreamResult(wadlOutBytes), TREE, XSD11, true, KEEP, true)
      }
      Then ("An exception should be thrown with the words 'missing-xinclude.wadl.xml',  'include', 'lineNumber: 16'")
      assert (thrown.getMessage().contains("missing-xinclude.wadl.xml"))
      assert (thrown.getMessage().contains("include"))
      assert (thrown.getMessage().contains("lineNumber: 16"))
    }

    scenario ("A WADL which references a WADL wich xincludes a missing file should fail with a correct error reported") {
      Given("a WADL wihch references a WADL with a missing xinclude")
      val wadlOutBytes = new ByteArrayOutputStream()
      When("the WADL is normalized")
      val thrown = intercept[Exception] {
        wadl.normalize(new StreamSource(new File("src/test/bad-test-samples/missing-xinclude2.wadl.xml")), new StreamResult(wadlOutBytes), TREE, XSD11, true, KEEP, true)
      }
      Then ("An exception should be thrown with the words 'missing-xinclude2.wadl.xml',  'include', 'lineNumber: 26'")
      assert (thrown.getMessage().contains("missing-xinclude-method.wadl.xml"))
      assert (thrown.getMessage().contains("include"))
      assert (thrown.getMessage().contains("lineNumber: 26"))
    }
  }
}
