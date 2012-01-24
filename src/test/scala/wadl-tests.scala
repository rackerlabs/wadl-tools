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
                <resource path="to/my/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                </resource>
              </resource>
           </resources>
        </application>
      when ("The wadl is normalized")
      val normWADL  = wadl.normalize(inWADL, TREE, XSD11, false, OMIT)
      then ("The extension attribute should be preserved")
      assert (normWADL, "//wadl:resource[@path='path' and @rax:invisible='true']")
    }

    scenario ("The original WADL contains paths prefixed with / to be converted to TREE format"){
	   given("a WADL with / prefixed paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="/{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
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
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
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

    scenario ("The original WADL contains paths prefixed with / to be converted to PATH format"){
	   given("a WADL with / prefixed paths in mixed mode")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="/{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
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
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
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
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="/{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
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
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
		  <resource path="{j}">
		   <param name="j" style="template" stype="xsd:string" required="true"/>
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
	<application xmlns="http://wadl.dev.java.net/2009/02">
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
		  <param name="j" style="template" stype="xsd:string" required="true"/>
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
	<method id="foo">
	  <response status="200 203"/>
	</method>
	</application>
      val outWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
              <resource path="a/b/c" id="rc">
	           <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
              <resource path="d/e" id="re">
	           <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
              <resource path="f" id="rf">
	           <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
	      <resource path="g" id="rg">
	           <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                       <response status="200 203"/>
                   </method>
	      </resource>
	      <resource path="h/i/{j}/k" id="rk">
                      <param name="j" xmlns:rax="http://docs.rackspace.com/api" style="template" stype="xsd:string" required="true" rax:id=""/>
	              <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                        <response status="200 203"/>
                      </method>
              </resource>
	      <resource path="h/i/{j}/k/l" id="rl">
                      <param name="j" xmlns:rax="http://docs.rackspace.com/api" style="template" stype="xsd:string" required="true" rax:id=""/>
	              <method xmlns:rax="http://docs.rackspace.com/api" rax:id="foo">
                        <response status="200 203"/>
                      </method>
	      </resource>
            </resources>
            <method id="foo">
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
