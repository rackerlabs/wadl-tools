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
class NormalizeWADLSpec extends BaseWADLSpec {

  feature ("The WADL normalizer can convert WADL resources into a tree format") {

    info("As a developer")
    info("I want to be able to convert all resources in a WADL into a tree format")
    info("So that I can process the WADL in a consistent fashion")

    scenario ("The original WADL is already in a tree format") {
      given("a WADL with resources in tree format")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
              <resource path="d">
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="g"/>
	      <resource path="h">
	      <resource path="i">
		<resource path="{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
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
            <method id="foo"/>
        </application>
      val outWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
              <resource path="d">
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="g"/>
	      <resource path="h">
	      <resource path="i">
		<resource path="{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
		   <resource path="k">
		      <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      <resource path="l">
			 <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      </resource>
		   </resource>
		</resource>
	      </resource>
	      </resource>
            </resources>
            <method id="foo"/>
        </application>
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      then("the resources should remain unchanged")
      canon(outWADL) should equal (canon(normWADL))
    }

    scenario ("The original WADL is already in a tree format and resource_types should be omitted") {
      given("a WADL with resources in tree format that uses resource_types")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
              <resource path="d">
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="g"/>
	      <resource path="h">
	      <resource path="i">
		<resource path="{j}">
		   <param name="j" style="template" stype="xsd:string" required="true">
	              <link resource_type="#rtype" rel="self"/>
		   </param>
		   <resource path="k">
		      <method href="#foo"/>
		      <resource path="l" type="#rtype"/>
		   </resource>
		</resource>
	      </resource>
	      </resource>
            </resources>
            <method id="foo"/>
	    <resource_type id="rtype">
               <method href="#foo"/>
            </resource_type> 
        </application>
      val outWADL = 
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c"/>
                </resource>
              </resource>
              <resource path="d">
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="g"/>
	      <resource path="h">
	      <resource path="i">
		<resource path="{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
		   <resource path="k">
		      <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      <resource path="l">
  		         <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
	              </resource>
		   </resource>
		</resource>
  	       </resource>
	      </resource>
            </resources>
            <method id="foo"/>
        </application>
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, OMIT)
      then("the resources should be the same except that resource_types and links to resource_types are omitted")
      canon(outWADL) should equal (canon(normWADL))
    }


    scenario ("The original WADL is in the path format"){
	given("a WADL with resources in path format")
	val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a/b/c">
	        <method href="#foo"/>
	      </resource>
              <resource path="d/e"/>
              <resource path="f"/>
	      <resource path="h/i/{j}/k">
	        <param name="j" style="template" stype="xsd:string" required="true"/>
	        <method href="#foo"/>
	      </resource>
	      <resource path="h/i/{j}/k/l">
	        <method href="#foo"/>		
	      </resource>
            </resources>
            <method id="foo"/>
        </application>
	val treeWADL = 
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c">
	             <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  </resource>
                </resource>
              </resource>
              <resource path="d">
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="h">
		 <resource path="i">
		    <resource path="{j}">
		       <param name="j" style="template" stype="xsd:string" required="true"/>
		       <resource path="k">
			  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
			  <resource path="l">
			     <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
			  </resource>
		       </resource>
		    </resource>
		 </resource>
	      </resource>
             </resources>
             <method id="foo"/>
        </application>
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      then("the resources should now be in tree format")
      canon(treeWADL) should equal (canon(normWADL))
    }

    scenario ("The original WADL is in the path format and resource_types should be omitted"){
	given("a WADL with resources in path format that uses resource_types")
	val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a/b/c" type="#rtype"/>
              <resource path="d/e"/>
              <resource path="f"/>
	      <resource path="h/i/{j}/k">
	        <param name="j" style="template" stype="xsd:string" required="true">
                  <link resource_type="#rtype" rel="self"/>
	        </param>
	        <method href="#foo"/>
	      </resource>
	      <resource path="h/i/{j}/k/l">
	        <method href="#foo"/>		
	      </resource>
            </resources>
            <method id="foo"/>
	    <resource_type id="rtype">
               <method href="#foo"/>
            </resource_type> 
        </application>
	val treeWADL = 
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a">
                <resource path="b">
                  <resource path="c">
	             <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  </resource>
                </resource>
              </resource>
              <resource path="d">
                <resource path="e"/>
              </resource>
              <resource path="f"/>
	      <resource path="h">
		 <resource path="i">
		    <resource path="{j}">
		       <param name="j" style="template" stype="xsd:string" required="true"/>
		       <resource path="k">
			  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
			  <resource path="l">
			     <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
			  </resource>
		       </resource>
		    </resource>
		 </resource>
	      </resource>
             </resources>
             <method id="foo"/>
        </application>
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, OMIT)
      then("the resources should now be in tree format")
      canon(treeWADL) should equal (canon(normWADL))
    }


    scenario ("The original WADL is in mixed path/tree format"){
	given("a WADL with resources in mixed path/tree format")
	val inWADL =
<application xmlns="http://wadl.dev.java.net/2009/02">
  <resources base="https://test.api.openstack.com">
    <resource path="a/b">
      <resource path="c">
	<method href="#foo"/>
      </resource>
    </resource>
    <resource path="d">
      <resource path="e/f"/>
    </resource>
    <resource path="g"/>
    <resource path="h/i/{j}/k">
      <param name="j" style="template" stype="xsd:string" required="true"/>
      <method href="#foo"/>
    </resource>
    <resource path="h/i/{j}/k/l">
      <method href="#foo"/>		
    </resource>
  </resources>
  <method id="foo"/>
</application>
	val treeWADL = 
<application xmlns="http://wadl.dev.java.net/2009/02">
   <resources base="https://test.api.openstack.com">
      <resource path="a">
         <resource path="b">
            <resource path="c">
               <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
            </resource>
         </resource>
      </resource>
      <resource path="d">
         <resource path="e">
            <resource path="f"/>
         </resource>
      </resource>
      <resource path="g"/>
      <resource path="h">
         <resource path="i">
            <resource path="{j}">
               <param name="j" style="template" stype="xsd:string" required="true"/>
               <resource path="k">
                  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  <resource path="l">
                     <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  </resource>
               </resource>
            </resource>
         </resource>
      </resource>
   </resources>
   <method id="foo"/>
</application>
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      then("the resources should now be in tree format")
      canon(treeWADL) should equal (canon(normWADL))
    }


    scenario ("The original WADL is in mixed path/tree format and resource_types should be omitted"){
	given("a WADL with resources in mixed path/tree format that uses resource_types")
	val inWADL =
<application xmlns="http://wadl.dev.java.net/2009/02">
  <resources base="https://test.api.openstack.com">
    <resource path="a/b">
      <resource path="c" type="#rtype"/>
    </resource>
    <resource path="d">
      <resource path="e/f"/>
    </resource>
    <resource path="g"/>
    <resource path="h/i/{j}/k">
      <param name="j" style="template" stype="xsd:string" required="true">
         <link resource_type="#rtype" rel="self"/>
      </param>
      <method href="#foo"/>
    </resource>
    <resource path="h/i/{j}/k/l">
      <method href="#foo"/>		
    </resource>
  </resources>
  <method id="foo"/>
   <resource_type id="rtype">
     <method href="#foo"/>
   </resource_type> 
</application>
	val treeWADL = 
<application xmlns="http://wadl.dev.java.net/2009/02">
   <resources base="https://test.api.openstack.com">
      <resource path="a">
         <resource path="b">
            <resource path="c">
               <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
            </resource>
         </resource>
      </resource>
      <resource path="d">
         <resource path="e">
            <resource path="f"/>
         </resource>
      </resource>
      <resource path="g"/>
      <resource path="h">
         <resource path="i">
            <resource path="{j}">
               <param name="j" style="template" stype="xsd:string" required="true"/>
               <resource path="k">
                  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  <resource path="l">
                     <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  </resource>
               </resource>
            </resource>
         </resource>
      </resource>
   </resources>
   <method id="foo"/>
</application>
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, true, OMIT)
      then("the resources should now be in tree format with resource_types and links to resource_types omitted")
      canon(treeWADL) should equal (canon(normWADL))
    }  

  }
}
