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
class NormalizeWADLEXTSpec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("rax", "http://docs.rackspace.com/api")

  feature ("The WADL normalizer can convert resources in a tree format while copying extension attributes") {

    info("As a developer")
    info("I want extension attributes to  be preserved correctly when I process a WADL into a tree format")
    info("So that that features that rely on extensions can continue to function after transformation")

    scenario ("The WADL contians extensions in refrences") {
      Given("Given semantically equivilant WADLs with and without method references")
        val inWADLNoReferences = <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
              <method name="PUT" rax:roles="a:observer"/>
              <resource path="/b" rax:roles="b:creator">
                  <method name="POST"/>
                  <method name="PUT" rax:roles="b:observer"/>
                  <method name="DELETE" rax:roles="b:observer b:admin"/>
              </resource>
           </resource>
          <resource path="/c/d" rax:roles="a:anotherRole"/>
        </resources>
       </application>

       val inWADLWithReferences = <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
            <resources base="https://test.api.openstack.com">
               <resource path="/a" rax:roles="a:admin">
                 <method href="#putOnA" rax:roles="a:observer"/>
                   <resource path="/b" rax:roles="b:creator">
                     <method href="#postOnB"/>
                     <method href="#putOnB"/>
                     <method href="#deleteOnB" rax:roles="b:observer b:admin"/>
                   </resource>
               </resource>
              <resource path="/c/d" rax:roles="a:anotherRole"/>
            </resources>
             <method id="putOnA" name="PUT"/>
             <method id="postOnB" name="POST"/>
             <method id="putOnB" name="PUT" rax:roles="b:observer"/>
             <method id="deleteOnB" name="DELETE" rax:roles="b:foo"/>
           </application>

      When ("When the WADLs are normalized in tree format")
      val normWADLNoRefs = wadl.normalize(inWADLNoReferences, TREE, XSD11, false, KEEP)
      val normWADLRefs = wadl.normalize(inWADLWithReferences, TREE, XSD11, false, KEEP)

      Then("XPaths to method reforences should match")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:method/@rax:roles = 'a:observer'")
      assert (normWADLNoRefs,"not(exists(/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='POST']/@rax:roles))")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='PUT']/@rax:roles = 'b:observer'")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='DELETE']/@rax:roles = 'b:observer b:admin'")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/@rax:roles = 'a:anotherRole'")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/@rax:roles != 'a:anotherRole' and @path = 'c'") //Raxroles should not apply to previous resources
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:method/@rax:roles = 'a:observer'")
      assert (normWADLRefs,"not(exists(/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='POST']/@rax:roles))")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='PUT']/@rax:roles = 'b:observer'")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='DELETE']/@rax:roles = 'b:observer b:admin'")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/@rax:roles = 'a:anotherRole'")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/@rax:roles != 'a:anotherRole' and @path = 'c'") //Raxroles should not apply to previous resources
    }
  }

}
