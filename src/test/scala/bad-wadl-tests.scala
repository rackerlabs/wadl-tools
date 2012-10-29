package com.rackspace.cloud.api.wadl.test

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.Converters._

import org.xml.sax.SAXParseException

@RunWith(classOf[JUnitRunner])
class BadWADLSpec extends BaseWADLSpec {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")

  feature ("The WADL normalizer should fail when an invalid WADL is supplied") {
    scenario ("A WADL with a link not containing a #, should be reqected") {
	   given("a WADL with a link missing a #")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words foo and #")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("#"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing link should be rejected") {
	   given("a WADL with a missing link")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'foo' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing external link should be rejected (missing file)") {
	   given("a WADL with a link missing external link")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'other.wadl#foo' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("other.wadl#foo"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing external link should be rejected") {
	   given("a WADL with a link missing external link")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'other.wadl#foo' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("other.wadl#foo"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with an external link of the wrong type should be rejected") {
	   given("a WADL with an external link of the wrong type")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'other.wadl#foo' and 'method'")
      assert(thrown.getMessage().contains("other.wadl#foo"))
      assert(thrown.getMessage().contains("method"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing include should be rejected") {
	   given("a WADL with a missing include")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'schema.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("schema.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a missing code sample should be rejected") {
	   given("a WADL with a missing code sample")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'missing_sample.xml' and 'is not available'.")
      assert(thrown.getMessage().contains("missing_sample.xml"))
      assert(thrown.getMessage().contains("is not available"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a parameter reference, that does not point to a parameter should be rejected") {
	   given("a WADL with a parameter reference pointing to a method")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'foo' and 'param'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("param"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a representation reference, that does not point to a representation should be rejected") {
	   given("a WADL with a representation reference pointing to a method")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'foo' and 'representation'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("representation"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a method reference, that does not point to a method should be rejected") {
	   given("a WADL with a method reference pointing to a representation")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'foo' and 'method'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("method"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource type reference, that does not point to a resource_type should be rejected") {
	   given("a WADL with a resource type  reference pointing to a representation")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'foo' and 'resource_type'")
      assert(thrown.getMessage().contains("foo"))
      assert(thrown.getMessage().contains("resource_type"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of references some of which are missing '#' should be rejected") {
	   given("a WADL with a resource with a set of  references some of which are missing '#'")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'foo jar' and 'missing '#''")
      assert(thrown.getMessage().contains("foo jar"))
      assert(thrown.getMessage().contains("missing '#'"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of references some of which are missing should be rejected") {
	   given("a WADL with a resource with a set of  references some of which are missing")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words '#not #here' and 'do not seem to exist in this wadl'")
      assert(thrown.getMessage().contains("#not #here"))
      assert(thrown.getMessage().contains("do not seem to exist in this wadl"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }


    scenario ("A WADL with a resource with a set of external references some of which are missing should be rejected") {
	   given("a WADL with a resource with a set of external references some of which are missing")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'other.wadl#not other.wadl#here' and 'do not seem to exist'")
      assert(thrown.getMessage().contains("other.wadl#not other.wadl#here"))
      assert(thrown.getMessage().contains("do not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of references some of which are pointing to the wrong type be rejected") {
	   given("a WADL with a resource with a set of  references some of which are pointing to the wrong type")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words '#jar' and 'are not pointing to a resource type.'")
      assert(thrown.getMessage().contains("'#jar'"))
      assert(thrown.getMessage().contains("are not pointing to a resource type."))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references some of which are pointing to the wrong type should be rejected") {
	   given("a WADL with a resource with a set of external references some of which are pointing to the wrong type")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'other.wadl#jar' and 'are not pointing to a resource type.'")
      assert(thrown.getMessage().contains("'other.wadl#jar'"))
      assert(thrown.getMessage().contains("are not pointing to a resource type."))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references some of which are missing, should be rejected (missing file)") {
	   given("a WADL with a resource with a set of external references some of which are missing")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'another.wadl#not another.wadl#here' and 'do not seem to exist'")
      assert(thrown.getMessage().contains("another.wadl#not another.wadl#here"))
      assert(thrown.getMessage().contains("do not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing resources") {
	   given("a WADL with a resource with a set of external references, the external wadl itself contains missing resources")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'myMethod' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("myMethod"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/other.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources") {
	   given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'myMethod' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("myMethod"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/foo/other.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources (missing file)") {
	   given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'myMethod' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("myMethod"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/foo/other.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources (two WADLs down)") {
	   given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'doGet' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("doGet"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/another.wadl"))
    }

    scenario ("A WADL with a resource with a set of external references, the external wadl itself contains missing external resources (two WADLs down, missing file)") {
	   given("a WADL with a resource with a set of external references, the external wadl itself contains missing external resources")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'doGet' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("doGet"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/another.wadl"))
    }

    scenario ("A WADL with an embeded schema, with a missing reference, should be rejected (import)") {
	   given("a WADL with an embeded schema schema with a missing reference")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with an embeded schema, with a missing reference, should be rejected (include)") {
	   given("a WADL with an embeded schema schema with a missing reference")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE)
      }
      then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://test/mywadl.wadl"))
    }

    scenario ("A WADL with an include that does not point to a schema should be rejected") {
	   given("a WADL with an include that does not point to a schema")
      register ("test://path/to/test/xsd/schema1.xsd", <junk />)
	   val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
             <grammars>
               <include href="xsd/schema1.xsd"/>
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }

      then("An exception should be thrown with the words 'schema1.xsd' and 'does not appear to be a valid XSD schema'.")
      assert(thrown.getMessage().contains("schema1.xsd"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSD schema"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with an include that does not point to a schema should be rejected (embeded)") {
	   given("a WADL with an include that does not point to a schema")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }

      then("An exception should be thrown with the words 'schema1.xsd' and 'does not appear to be a valid XSD schema'.")
      assert(thrown.getMessage().contains("schema1.xsd"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSD schema"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/mywadl.wadl"))
    }

    scenario ("A WADL with an embeded schema, which contains a schema that has a missing reference, should be rejected") {
	   given("a WADL with an embeded schema, the linked schema is missing a reference")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/xsd/schema1.xsd"))
    }

    scenario ("A WADL with an embeded schema, which contains a schema that has a missing reference, should be rejected (two levels)") {
	   given("a WADL with an embeded schema, the linked schema is missing a reference")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'b.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("c.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/xsd/b.xsd"))
    }

    scenario ("A WADL with an embeded schema, which contains a schema that is referencing a non-schema, should be rejected") {
	   given("a WADL with an embeded schema, that contains a schema this is referencing a non-schema")
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
      when("the WADL is normalized")
      val thrown = intercept[Exception] {
        val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, KEEP)
      }
      then("An exception should be thrown with the words 'b.xsd' and 'does not appear to be a valid XSD schema'.")
      assert(thrown.getMessage().contains("b.xsd"))
      assert(thrown.getMessage().contains("does not appear to be a valid XSD schema"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://path/to/test/xsd/schema1.xsd"))
    }
  }
}
