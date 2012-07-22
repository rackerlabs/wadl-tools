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

    scenario ("The original WADL is already in a tree format") {
      given("a WADL with resources in tree format")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a" queryType="application/x-www-form-urlencoded">
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
      val normWADL = wadl.normalize(inWADL, TREE)
      assertWADL(normWADL)
      assertWADL(outWADL)
      then("the resources should remain unchanged")
      canon(outWADL) should equal (canon(normWADL))
    }

    scenario ("The original WADL is already in a tree format and resource_types should be omitted") {
      given("a WADL with resources in tree format that uses resource_types")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
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
		   <param name="j" style="template" type="xsd:string" required="true">
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
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource queryType="application/x-www-form-urlencoded" path="a">
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
		      <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
		      <resource queryType="application/x-www-form-urlencoded" path="l">
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
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a/b/c">
	        <method href="#foo"/>
	      </resource>
              <resource path="d/e"/>
              <resource path="f"/>
	      <resource path="h/i/{j}/k">
	        <param name="j" style="template" type="xsd:string" required="true"/>
	        <method href="#foo"/>
	      </resource>
	      <resource path="h/i/{j}/k/l">
	        <method href="#foo"/>		
	      </resource>
            </resources>
            <method id="foo"/>
        </application>
	val treeWADL = 
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource queryType="application/x-www-form-urlencoded" path="a">
                <resource queryType="application/x-www-form-urlencoded" path="b">
                  <resource queryType="application/x-www-form-urlencoded" path="c">
	             <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  </resource>
                </resource>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="d">
                <resource queryType="application/x-www-form-urlencoded" path="e"/>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="f"/>
	      <resource queryType="application/x-www-form-urlencoded" path="h">
		 <resource queryType="application/x-www-form-urlencoded" path="i">
		    <resource queryType="application/x-www-form-urlencoded" path="{j}">
		       <param name="j" style="template" type="xsd:string" required="true" repeating="false"/>
		       <resource queryType="application/x-www-form-urlencoded" path="k">
			  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
			  <resource queryType="application/x-www-form-urlencoded" path="l">
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
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a/b/c" type="#rtype"/>
              <resource path="d/e"/>
              <resource path="f"/>
	      <resource path="h/i/{j}/k">
	        <param name="j" style="template" type="xsd:string" required="true">
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
        <application xmlns="http://wadl.dev.java.net/2009/02"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource queryType="application/x-www-form-urlencoded" path="a">
                <resource queryType="application/x-www-form-urlencoded" path="b">
                  <resource queryType="application/x-www-form-urlencoded" path="c">
	             <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  </resource>
                </resource>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="d">
                <resource queryType="application/x-www-form-urlencoded" path="e"/>
              </resource>
              <resource queryType="application/x-www-form-urlencoded" path="f"/>
	      <resource queryType="application/x-www-form-urlencoded" path="h">
		 <resource queryType="application/x-www-form-urlencoded" path="i">
		    <resource queryType="application/x-www-form-urlencoded" path="{j}">
		       <param name="j" style="template" type="xsd:string" required="true" repeating="false"/>
		       <resource queryType="application/x-www-form-urlencoded" path="k">
			  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
			  <resource queryType="application/x-www-form-urlencoded" path="l">
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
<application xmlns="http://wadl.dev.java.net/2009/02"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
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
      <param name="j" style="template" type="xsd:string" required="true"/>
      <method href="#foo"/>
    </resource>
    <resource path="h/i/{j}/k/l">
      <method href="#foo"/>		
    </resource>
  </resources>
  <method id="foo"/>
</application>
	val treeWADL = 
<application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
   <resources base="https://test.api.openstack.com">
      <resource queryType="application/x-www-form-urlencoded" path="a">
         <resource queryType="application/x-www-form-urlencoded" path="b">
            <resource queryType="application/x-www-form-urlencoded" path="c">
               <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
            </resource>
         </resource>
      </resource>
      <resource queryType="application/x-www-form-urlencoded" path="d">
         <resource queryType="application/x-www-form-urlencoded" path="e">
            <resource queryType="application/x-www-form-urlencoded" path="f"/>
         </resource>
      </resource>
      <resource queryType="application/x-www-form-urlencoded" path="g"/>
      <resource queryType="application/x-www-form-urlencoded" path="h">
         <resource queryType="application/x-www-form-urlencoded" path="i">
            <resource queryType="application/x-www-form-urlencoded" path="{j}">
               <param name="j" style="template" type="xsd:string" required="true" repeating="false"/>
               <resource queryType="application/x-www-form-urlencoded" path="k">
                  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  <resource queryType="application/x-www-form-urlencoded" path="l">
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
<application xmlns="http://wadl.dev.java.net/2009/02"
     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <resources base="https://test.api.openstack.com">
    <resource path="a/b">
      <resource path="c" type="#rtype"/>
    </resource>
    <resource path="d">
      <resource path="e/f"/>
    </resource>
    <resource path="g"/>
    <resource path="h/i/{j}/k">
      <param name="j" style="template" type="xsd:string" required="true">
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
<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
   <resources base="https://test.api.openstack.com">
      <resource queryType="application/x-www-form-urlencoded" path="a">
         <resource queryType="application/x-www-form-urlencoded" path="b">
            <resource queryType="application/x-www-form-urlencoded" path="c">
               <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
            </resource>
         </resource>
      </resource>
      <resource queryType="application/x-www-form-urlencoded" path="d">
         <resource queryType="application/x-www-form-urlencoded" path="e">
            <resource queryType="application/x-www-form-urlencoded" path="f"/>
         </resource>
      </resource>
      <resource queryType="application/x-www-form-urlencoded" path="g"/>
      <resource queryType="application/x-www-form-urlencoded" path="h">
         <resource queryType="application/x-www-form-urlencoded" path="i">
            <resource queryType="application/x-www-form-urlencoded" path="{j}">
               <param name="j" style="template" type="xsd:string" required="true" repeating="false"/>
               <resource queryType="application/x-www-form-urlencoded" path="k">
                  <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo"/>
                  <resource queryType="application/x-www-form-urlencoded" path="l">
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

    scenario ("The original WADL contains an extension attribute") {
      given("a WADL with an extension attribute")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource rax:invisible="true" path="path">
               <method name="GET">
                    <response status="200 203"/>
                </method>
                <resource path="to/my/resource" rax:invisible="true">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
		               <rax:foo/>
                </resource>
              </resource>
           </resources>
        </application>
      when ("The wadl is normalized")
      val normWADL  = wadl.normalize(inWADL, TREE, XSD11, false, OMIT)
      then ("The extension attribute should be preserved")
      assert (normWADL, "//wadl:resource[@path='path' and @rax:invisible='true']")
      assert (normWADL, "//wadl:resource[@path='to' and @rax:invisible='true']")
      assert (normWADL, "//wadl:resource[@path='my' and @rax:invisible='true']")
      assert (normWADL, "//wadl:resource[@path='resource' and @rax:invisible='true']")
      assert (normWADL, "//wadl:resource[@path='to']/rax:foo")
      assert (normWADL, "//wadl:resource[@path='my']/rax:foo")
      assert (normWADL, "//wadl:resource[@path='resource']/rax:foo")

    }

    scenario ("The original WADL contains an extension element in the rax: namespace with an href attribute") {
      given("a WADL with an extension extension element in the rax: namespace with an href attribute")
      val inWADL = ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
               <method name="GET">
                    <response status="200 203"/>
                </method>
                <resource path="to/my/resource" rax:invisible="true">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                     <rax:log href="my_log.txt"/>
                </resource>
              </resource>
           </resources>
        </application>)
      when ("The wadl is normalized")
      val normWADL  = wadl.normalize(inWADL, TREE, XSD11, false, OMIT)
      then ("The extension elemest should be preserved and the relative href should be expanded")
      assert (normWADL, "//wadl:resource[@path='resource']/rax:log")
      assert (normWADL, "//wadl:resource[@path='resource']/rax:log/@href = 'test://path/to/test/my_log.txt'")
    }

    scenario ("The original WADL contains paths prefixed with / to be converted to TREE format"){
	   given("a WADL with / prefixed paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="/{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource path="/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		</resource>
              </resource>
           </resources>
        </application>
      and ("a WADL without the / prefix")
      val inWADL2 =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		 </resource>
              </resource>
           </resources>
        </application>
      then("the normalize wadls should be equivalent if converted to TREE format")
      canon(wadl.normalize(inWADL, TREE, XSD11, true, OMIT)) should equal (canon(wadl.normalize(inWADL2, TREE, XSD11, true, OMIT)))
    }

    scenario ("The original WADL contains paths starting and ending  with / to be converted to PATH format"){
	   given("a WADL with / prefixed paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/">
		  <resource path="/{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		  </resource>
              </resource>
           </resources>
        </application>
      and ("a WADL without the / prefix")
      val inWADL2 =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
              </resource>
           </resources>
        </application>
      then("the normalize wadls should be equivalent if converted to PATH format")
      canon(wadl.normalize(inWADL, PATH, XSD11, true, OMIT)) should equal (canon(wadl.normalize(inWADL2, PATH, XSD11, true, OMIT)))
    }

    scenario ("The original WADL contains paths prefixed with / to with the format unchanged"){
	   given("a WADL with / prefixed paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="/{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		  </resource>
              </resource>
           </resources>
        </application>
      and ("a WADL without the / prefix")
      val inWADL2 =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
              </resource>
           </resources>
        </application>
      then("the normalize wadls should be equivalent if the format is unchnaged")
      canon(wadl.normalize(inWADL, DONT, XSD11, true, OMIT)) should equal (canon(wadl.normalize(inWADL2, DONT, XSD11, true, OMIT)))
    }

    //
    //  The following scenarios test a custom type template parameter at the
    //  of a resource path (/path/to/my/resource/{yn}. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //

    def customTemplateAtEndAssertions (normWADL : NodeSeq) : Unit = {
      then ("The param should have a valid QName")
      assert (normWADL, "namespace-uri-from-QName(resolve-QName(//wadl:param[@name='yn'][1]/@type, //wadl:param[@name='yn'][1])) "+
                                           "= 'test://schema/a'")
      assert (normWADL, "local-name-from-QName(resolve-QName(//wadl:param[@name='yn'][1]/@type, //wadl:param[@name='yn'][1])) "+
                                           "= 'yesno'")
      assert (normWADL, "local-name-from-QName(resolve-QName(//wadl:resource[@path='{yn}'][1]/wadl:method/wadl:request/wadl:representation/@element, //wadl:resource[@path='{yn}'][1]/wadl:method/wadl:request/wadl:representation)) " + 
					  "= 'credentials'")
      and ("The grammar files shoud remain included")
      assert (normWADL, "/wadl:application/wadl:grammars/wadl:include/@href = 'test://simple.xsd'")
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path") {
      given("A WADL with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
	        <request>
	  	  <representation mediaType="application/xml" element="credentials"/>
	        </request>
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      customTemplateAtEndAssertions(normWADL)
    }

    scenario("The WADL in tree format contains a template parameter of a custom type at the end of the path") {
      given("A WADL in tree format with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                    <resource path="{yn}">
                       <param name="yn" style="template" type="tst:yesno"/>
                       <method href="#getMethod" />
                    </resource>
                  </resource>
                </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
	        <request>
	  	  <representation mediaType="application/xml" element="credentials"/>
	        </request>
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      customTemplateAtEndAssertions(normWADL)
    }

    scenario("The WADL in mix format contains a template parameter of a custom type at the end of the path") {
      given("A WADL in mix format with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                   <resource path="resource">
                    <resource id="yn" path="{yn}">
                       <param name="yn" style="template" type="tst:yesno"/>
                       <method href="#getMethod" />
                    </resource>
                    </resource>
               </resource>
           </resources>
           <method id="getMethod" name="GET">
	        <request>
	  	  <representation mediaType="application/xml" element="credentials"/>
	        </request>
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      customTemplateAtEndAssertions(normWADL)
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL with a template parameter of a custom type at the end of the path, with the type in a default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                          xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://simple.xsd"/>
           </wadl:grammars>
           <wadl:resources base="https://test.api.openstack.com">
              <wadl:resource id="yn" path="path/to/my/resource/{yn}">
                   <wadl:param name="yn" style="template" type="yesno"/>
                   <wadl:method href="#getMethod" />
              </wadl:resource>
           </wadl:resources>
           <wadl:method id="getMethod" name="GET">
	        <wadl:request>
	  	  <wadl:representation mediaType="application/xml" element="credentials"/>
	        </wadl:request>
               <wadl:response status="200 203"/>
           </wadl:method>
        </wadl:application>
      register("test://simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      customTemplateAtEndAssertions(normWADL)
    }

    scenario("The WADL in tree format contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL in tree format with a template parameter of a custom type at the end of the path, the type is in the default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                          xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://simple.xsd"/>
           </wadl:grammars>
           <wadl:resources base="https://test.api.openstack.com">
              <wadl:resource path="path">
                <wadl:resource path="to">
                  <wadl:resource path="my">
                   <wadl:resource path="resource">
                    <wadl:resource path="{yn}">
                       <wadl:param name="yn" style="template" type="yesno"/>
                       <wadl:method href="#getMethod" />
                    </wadl:resource>
                  </wadl:resource>
                </wadl:resource>
               </wadl:resource>
             </wadl:resource>
           </wadl:resources>
           <wadl:method id="getMethod" name="GET">
	        <wadl:request>
	  	  <wadl:representation mediaType="application/xml" element="credentials"/>
	        </wadl:request>
               <wadl:response status="200 203"/>
           </wadl:method>
        </wadl:application>
      register("test://simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      customTemplateAtEndAssertions(normWADL)
    }

    scenario("The WADL in mix format contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL in mix format with a template parameter of a custom type at the end of the path, the type is in the default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                         xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://simple.xsd"/>
           </wadl:grammars>
           <wadl:resources base="https://test.api.openstack.com">
              <wadl:resource path="path/to/my">
                   <wadl:resource path="resource">
                    <wadl:resource id="yn" path="{yn}">
                       <wadl:param name="yn" style="template" type="yesno"/>
                       <wadl:method href="#getMethod" />
                    </wadl:resource>
                    </wadl:resource>
               </wadl:resource>
           </wadl:resources>
           <wadl:method id="getMethod" name="GET">
	        <wadl:request>
	  	  <wadl:representation mediaType="application/xml" element="credentials"/>
	        </wadl:request>
               <wadl:response status="200 203"/>
           </wadl:method>
        </wadl:application>
      register("test://simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE)
      customTemplateAtEndAssertions(normWADL)
    }

    scenario ("The original WADL contains paths ending with / to be converted to TREE format"){
	   given("a WADL with / ending paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource path="/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		</resource>
              </resource>
           </resources>
        </application>
      and ("a WADL without ending in /")
      val inWADL2 =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		 </resource>
              </resource>
           </resources>
        </application>
      then("the normalize wadls should be equivalent if converted to TREE format")
      canon(wadl.normalize(inWADL, TREE, XSD11, true, OMIT)) should equal (canon(wadl.normalize(inWADL2, TREE, XSD11, true, OMIT)))
    }

    scenario ("The original WADL contains paths ending with / to be converted to PATH format"){
	   given("a WADL with / ending paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		  </resource>
              </resource>
           </resources>
        </application>
      and ("a WADL without the / ending")
      val inWADL2 =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
              </resource>
           </resources>
        </application>
      then("the normalize wadls should be equivalent if converted to PATH format")
      canon(wadl.normalize(inWADL, PATH, XSD11, true, OMIT)) should equal (canon(wadl.normalize(inWADL2, PATH, XSD11, true, OMIT)))
    }

    scenario ("The original WADL contains paths ending with / to with the format unchanged"){
	   given("a WADL with / ending paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
		  </resource>
              </resource>
           </resources>
        </application>
      and ("a WADL without the / ending")
      val inWADL2 =
        <application xmlns="http://wadl.dev.java.net/2009/02"
           xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" type="xsd:string" required="true"/>
                   <resource id="foo" path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
              </resource>
           </resources>
        </application>
      then("the normalize wadls should be equivalent if the format is unchnaged")
      canon(wadl.normalize(inWADL, DONT, XSD11, true, OMIT)) should equal (canon(wadl.normalize(inWADL2, DONT, XSD11, true, OMIT)))
    }
  }

  feature ("The WADL normalizer can convert WADL resources into a path format") {

    info("As a developer")
    info("I want to be able to convert all resources in a WADL into a path format")
    info("So that I can process the WADL in a consistent fashion and use the wadl to produce DocBook")

    scenario ("The original WADL is in a tree format") {
      given("a WADL with resources in tree format")
      val inWADL =
	<application xmlns="http://wadl.dev.java.net/2009/02"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
	  <resources base="https://test.api.openstack.com">
	    <resource path="a">
	      <resource path="b">
		<resource path="c" id="rc">
		  <method href="#foo"/>		
		</resource>
	      </resource>
	    </resource>
	    <resource path="d">
	      <resource path="e" id="re">
		<method href="#foo"/>
	      </resource>
	    </resource>
	    <resource path="f" id="rf">
	      <method href="#foo"/>
	    </resource>
	    <resource path="g" id="rg">
	      <method href="#foo"/>
	    </resource>    
	    <resource path="h">
	      <resource path="i">
		<resource path="{j}">
		  <param name="j" style="template" type="xsd:string" required="true"/>
		  <resource path="k" id="rk">
		    <method href="#foo"/>
		    <resource path="l" id="rl">
		      <method href="#foo"/>
		    </resource>
		  </resource>
		</resource>
	      </resource>
	    </resource>
	</resources>
	<method name="GET" id="foo">
	  <response status="200 203"/>
	</method>
	</application>
      val outWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <resources base="https://test.api.openstack.com">
              <resource path="a/b/c" id="rc" queryType="application/x-www-form-urlencoded">
	           <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
              <resource path="d/e" id="re" queryType="application/x-www-form-urlencoded">
	           <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
              <resource path="f" id="rf" queryType="application/x-www-form-urlencoded">
	           <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
	      <resource path="g" id="rg" queryType="application/x-www-form-urlencoded">
	           <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
	      <resource path="h/i/{j}/k" id="rk" queryType="application/x-www-form-urlencoded">
                      <param name="j" xmlns:rax="http://docs.rackspace.com/api" style="template" type="xsd:string" required="true" rax:id="" repeating="false"/>
	              <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                        <response status="200 203"/>
                      </method>
              </resource>
	      <resource path="h/i/{j}/k/l" id="rl" queryType="application/x-www-form-urlencoded">
                      <param name="j" xmlns:rax="http://docs.rackspace.com/api" style="template" type="xsd:string" required="true" rax:id="" repeating="false"/>
	              <method name="GET" xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                        <response status="200 203"/>
                      </method>
	      </resource>
            </resources>
            <method name="GET" id="foo">
                 <response status="200 203"/>
            </method>
        </application>
      when("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, PATH)
      then("the paths in the resource should be flattened")
      canon(outWADL) should equal (canon(normWADL))
    }



  }

}
