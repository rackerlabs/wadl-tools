package com.rackspace.cloud.api.wadl.test

import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TreeMethodIDWADLSpec extends BaseWADLSpec {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("rax", "http://docs.rackspace.com/api")

  feature ("The WADL normalizer can convert WADL resources into a tree format") {
    info("As a developer")
    info("I want to be able to convert all resources in a WADL into a tree format")
    info("So that I can process the WADL in a consistent fashion")

    //
    //  Used by most examples...
    //
    val base =
        "/wadl:application/wadl:resources/wadl:resource[@path='path']/wadl:resource[@path='to']/wadl:resource[@path='my']/wadl:resource[@path='resource']"

    scenario ("The original WADL has method IDs and no references") {
      Given("a WADL with methodIDs and no references to them")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
          <grammars/>
            <resources base="https://test.api.openstack.com">
                <resource path="path/to/my/resource">
                  <method id="action1" name="POST">
                    <response status="201"/>
                  </method>
                  <method id="action2" name="POST">
                    <response status="201"/>
                  </method>
                  <method id="action3" name="POST">
                    <response status="201"/>
                  </method>
                  <method id="action4" name="POST">
                   <response status="201"/>
                  </method>
                </resource>
            </resources>
     </application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The method IDs should be replaced with rax:ids in the normalized WADL")

      assert(normWADL, base+"/wadl:method[@rax:id='action1']")
      assert(normWADL, base+"/wadl:method[@rax:id='action2']")
      assert(normWADL, base+"/wadl:method[@rax:id='action3']")
      assert(normWADL, base+"/wadl:method[@rax:id='action4']")
      assert(normWADL, "not("+base+"/wadl:method[@id='action1'])")
      assert(normWADL, "not("+base+"/wadl:method[@id='action2'])")
      assert(normWADL, "not("+base+"/wadl:method[@id='action3'])")
      assert(normWADL, "not("+base+"/wadl:method[@id='action4'])")
    }

    scenario ("The original WADL has method IDs in a resource type") {
      Given ("a WADL with methodID in a resource type")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
          <grammars/>
          <resources base="https://test.api.openstack.com">
            <resource path="path/to/my/resource" type="#test"/>
          </resources>
          <resource_type id="test">
            <method id="getMethod" name="GET">
              <response status="200 203"/>
            </method>
            <method name="DELETE">
              <response status="200"/>
            </method>
            <method name="POST">
              <request>
                <representation mediaType="application/xml"/>
              </request>
              <response status="200"/>
            </method>
          </resource_type>
        </application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The method IDs should be replaced with rax:ids in resources, but should be kept in resource types")
      assert(normWADL, base+"/wadl:method[@rax:id='getMethod']")
      assert(normWADL, "not("+base+"/wadl:method[@id='getMethod'])")
      assert(normWADL, "/wadl:application/wadl:resource_type[@id='test']/wadl:method[@id='getMethod']")
      assert(normWADL, "not(/wadl:application/wadl:resource_type[@id='test']/wadl:method[@rax:id='getMethod'])")
    }

    scenario ("The original WADL has global MethodIDs refernced by a resource_type.") {
      Given ("a WADL with a global method ID reference by a resource type")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
          <grammars/>
          <resources base="https://test.api.openstack.com">
            <resource path="path/to/my/resource" type="#test"/>
          </resources>
          <resource_type id="test">
            <method href="#getMethod"/>
            <method name="DELETE">
              <response status="200"/>
            </method>
            <method name="POST">
              <request>
                <representation mediaType="application/xml"/>
              </request>
              <response status="200"/>
            </method>
          </resource_type>
          <method id="getMethod" name="GET">
             <response status="200 203"/>
          </method>
        </application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The method IDs should be replaced with rax:ids in resources, and resource_type but should be kept globally")
      assert(normWADL, base+"/wadl:method[@rax:id='getMethod']")
      assert(normWADL, "not("+base+"/wadl:method[@id='getMethod'])")
      assert(normWADL, "/wadl:application/wadl:resource_type[@id='test']/wadl:method[@rax:id='getMethod']")
      assert(normWADL, "not(/wadl:application/wadl:resource_type[@id='test']/wadl:method[@id='getMethod'])")
      assert(normWADL, "/wadl:application/wadl:method[@id='getMethod']")
      assert(normWADL, "not(/wadl:application/wadl:method[@rax:id='getMethod'])")
    }

    scenario ("The original WADL has global and local MethodIDs refernced in a resource_type.") {
      Given ("a WADL with a global and local method IDs reference in a resource type")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
          <grammars/>
          <resources base="https://test.api.openstack.com">
            <resource path="path/to/my/resource" type="#test"/>
          </resources>
          <resource_type id="test">
            <method href="#getMethod"/>
            <method id="delete" name="DELETE">
              <response status="200"/>
            </method>
            <method name="POST">
              <request>
                <representation mediaType="application/xml"/>
              </request>
              <response status="200"/>
            </method>
          </resource_type>
          <method id="getMethod" name="GET">
             <response status="200 203"/>
          </method>
        </application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The method IDs should be replaced with rax:ids in resources, and resource_type but should be kept globally")
      assert(normWADL, base+"/wadl:method[@rax:id='getMethod']")
      assert(normWADL, "not("+base+"/wadl:method[@id='getMethod'])")
      assert(normWADL, "/wadl:application/wadl:resource_type[@id='test']/wadl:method[@rax:id='getMethod']")
      assert(normWADL, "not(/wadl:application/wadl:resource_type[@id='test']/wadl:method[@id='getMethod'])")
      assert(normWADL, "/wadl:application/wadl:resource_type[@id='test']/wadl:method[@id='delete']")
      assert(normWADL, "not(/wadl:application/wadl:resource_type[@id='test']/wadl:method[@rax:id='delete'])")
      assert(normWADL, "/wadl:application/wadl:method[@id='getMethod']")
      assert(normWADL, "not(/wadl:application/wadl:method[@rax:id='getMethod'])")
    }
  }
}
