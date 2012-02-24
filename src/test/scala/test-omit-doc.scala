package com.rackspace.cloud.api.wadl.test

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.DOCType._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.Converters._

@RunWith(classOf[JUnitRunner])
class OmitWADLDocumentation extends BaseWADLSpec {


  feature ("The WADL normalizer can omit or retain documentation") {

    info("As a developer")
    info("I want to be able to retain or omit all documentation in a WADL")

    scenario ("The original WADL contains documentation and I want to keep it") {
      given("a WADL with documentation")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a" queryType="application/x-www-form-urlencoded">
                <doc xml:lang="EN" title="Detail Image List">
                    <p xmlns="http://www.w3.org/1999/xhtml">
                        A Random comment
                    </p>
                </doc>
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
              <resource path="d">
                <doc xml:lang="EN" title="Detail Image List">
                    <p xmlns="http://www.w3.org/1999/xhtml">
                        Another Random comment
                    </p>
                </doc>              
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="g"/>
	      <resource path="h">
	      <resource path="i">
		<resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
		   <resource path="k">
		      <method href="#foo"/>
		      <resource path="l">
			 <method href="#foo"/>
		      </resource>
		   </resource>
		</resource>
	      </resource>
	      </resource>
            </resources>
            <method name="GET" id="foo"/>
        </application>
      val outWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a" queryType="application/x-www-form-urlencoded">
                <resource queryType="application/x-www-form-urlencoded" path="b">
                  <resource queryType="application/x-www-form-urlencoded" path="c"/>
                </resource>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="d">
                <resource queryType="application/x-www-form-urlencoded" path="e"/>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="f"/>
	      <resource queryType="application/x-www-form-urlencoded" path="g"/>
	      <resource queryType="application/x-www-form-urlencoded" path="h">
	      <resource queryType="application/x-www-form-urlencoded" path="i">
		<resource queryType="application/x-www-form-urlencoded" path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true" repeating="false"/>
		   <resource queryType="application/x-www-form-urlencoded" path="k">
		      <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      <resource queryType="application/x-www-form-urlencoded" path="l">
			 <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      </resource>
		   </resource>
		</resource>
	      </resource>
	      </resource>
            </resources>
            <method name="GET" id="foo"/>
        </application>
        
        
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEPIT, KEEP)
      assertWADL(normWADL)
      assertWADL(outWADL)
      then("the resources should remain unchanged")
      canon(outWADL) should not equal (canon(normWADL))
      
    }


    scenario ("The original WADL contains documentation and we want to omit it") {
      given("a WADL with documentation")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a" queryType="application/x-www-form-urlencoded">
                <doc xml:lang="EN" title="Detail Image List">
                    <p xmlns="http://www.w3.org/1999/xhtml">
                        A Random comment
                    </p>
                </doc>
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
              <resource path="d">
                <doc xml:lang="EN" title="Detail Image List">
                    <p xmlns="http://www.w3.org/1999/xhtml">
                        Another Random comment
                    </p>
                </doc>              
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="g"/>
	      <resource path="h">
	      <resource path="i">
		<resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
		   <resource path="k">
		      <method href="#foo"/>
		      <resource path="l">
			 <method href="#foo"/>
		      </resource>
		   </resource>
		</resource>
	      </resource>
	      </resource>
            </resources>
            <method name="GET" id="foo"/>
        </application>
      val outWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a" queryType="application/x-www-form-urlencoded">
                <resource queryType="application/x-www-form-urlencoded" path="b">
                  <resource queryType="application/x-www-form-urlencoded" path="c"/>
                </resource>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="d">
                <resource queryType="application/x-www-form-urlencoded" path="e"/>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="f"/>
	      <resource queryType="application/x-www-form-urlencoded" path="g"/>
	      <resource queryType="application/x-www-form-urlencoded" path="h">
	      <resource queryType="application/x-www-form-urlencoded" path="i">
		<resource queryType="application/x-www-form-urlencoded" path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true" repeating="false"/>
		   <resource queryType="application/x-www-form-urlencoded" path="k">
		      <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      <resource queryType="application/x-www-form-urlencoded" path="l">
			 <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      </resource>
		   </resource>
		</resource>
	      </resource>
	      </resource>
            </resources>
            <method name="GET" id="foo"/>
        </application>
        
        
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, OMITIT, KEEP)
      assertWADL(normWADL)
      assertWADL(outWADL)
      then("the resources should remain unchanged")
      canon(outWADL) should equal (canon(normWADL))
      
    }



  }
}