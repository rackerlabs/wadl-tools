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
class NormalizeWADLNSSpec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("rax", "http://docs.rackspace.com/api")

  feature ("The WADL normalizer can convert WADL resources into a tree format while preserving namespaces") {

    info("As a developer")
    info("I want namespaces to be preserved correctly when I process a WADL into a tree format")
    info("So that that features that rely on namespaces can continue to function after transformation")

    //
    //  The following scenarios test the element attribute in a
    //  representation.
    //

    scenario("The WADL contains a XML representation with an element definition. NS defined in root") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS defined in resources") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS defined in resource") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS defined in request") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS defined in representation") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in representation") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in request") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in resource") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in resources") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in method") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in sep method") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                   <wadl:method href="#doPOST"/>
               </wadl:resource>
             </wadl:resources>
             <wadl:method name="POST" id="doPOST"
                     xmlns:tst="http://www.rackspace.com/wadl/test">
                 <wadl:request>
                     <wadl:representation mediaType="application/xml" element="tst:a">
                     </wadl:representation>
                 </wadl:request>
             </wadl:method>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in external method") {
      Given("a WADL with an element definition")
      val inWADL =("test://path/to/test/my.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                   <wadl:method href="other.wadl#doPOST"/>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:method name="POST" id="doPOST"
                     xmlns:tst="http://www.rackspace.com/wadl/test">
                 <wadl:request>
                     <wadl:representation mediaType="application/xml" element="tst:a">
                     </wadl:representation>
                 </wadl:request>
             </wadl:method>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in external application (method)") {
      Given("a WADL with an element definition")
      val inWADL =("test://path/to/test/my.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                   <wadl:method href="other.wadl#doPOST"/>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="http://www.rackspace.com/wadl/test">
             <wadl:method name="POST" id="doPOST">
                 <wadl:request>
                     <wadl:representation mediaType="application/xml" element="tst:a">
                     </wadl:representation>
                 </wadl:request>
             </wadl:method>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in resource-type") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="#doPOST">
               </wadl:resource>
             </wadl:resources>
             <wadl:resource_type id="doPOST"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in resource-type/method") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="#doPOST">
               </wadl:resource>
             </wadl:resources>
             <wadl:resource_type id="doPOST">
                  <wadl:method name="POST"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in resource-type/representation") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="#doPOST">
               </wadl:resource>
             </wadl:resources>
             <wadl:resource_type id="doPOST">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a"
                             xmlns:tst="http://www.rackspace.com/wadl/test">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in external resource-type") {
      Given("a WADL with an element definition")
      val inWADL =("test://path/to/test/mywadl.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="other.wadl#doPOST">
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:resource_type id="doPOST" xmlns:tst="http://www.rackspace.com/wadl/test">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in external application") {
      Given("a WADL with an element definition")
      val inWADL =("test://path/to/test/mywadl.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="other.wadl#doPOST">
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02" xmlns:tst="http://www.rackspace.com/wadl/test">
             <wadl:resource_type id="doPOST">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in external request") {
      Given("a WADL with an element definition")
      val inWADL =("test://path/to/test/mywadl.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="other.wadl#doPOST">
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:resource_type id="doPOST">
                  <wadl:method name="POST">
                     <wadl:request xmlns:tst="http://www.rackspace.com/wadl/test">
                         <wadl:representation mediaType="application/xml" element="tst:a">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    scenario("The WADL contains a XML representation with an element definition. NS re-defined in external representation") {
      Given("a WADL with an element definition")
      val inWADL =("test://path/to/test/mywadl.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="other.wadl#doPOST">
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:resource_type id="doPOST">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a"  xmlns:tst="http://www.rackspace.com/wadl/test">
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
    }

    //
    //   The following are more complex scenarios ensure that namespaces work with plain
    //   params, and extensions.
    //

    scenario("The WADL contains a XML representation with an element definition, and plain params. All NS defined in root.") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                             xmlns:tst="http://www.rackspace.com/wadl/test"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                             xmlns:rax="http://docs.rackspace.com/api">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                              <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"/>
                              <rax:foo bla="tst3:c">
                              </rax:foo>
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[@element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS defined locally") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a" xmlns:tst="http://www.rackspace.com/wadl/test">
                              <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"
                                          xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                          xmlns:tst2="http://www.rackspace.com/wadl/test/2"/>
                              <rax:foo bla="tst3:c" xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                                                    xmlns:rax="http://docs.rackspace.com/api">
                              </rax:foo>
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[@element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined locally") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a" xmlns:tst="http://www.rackspace.com/wadl/test">
                              <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"
                                          xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                          xmlns:tst2="http://www.rackspace.com/wadl/test/2"/>
                              <rax:foo bla="tst3:c" xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                                                    xmlns:rax="http://docs.rackspace.com/api">
                              </rax:foo>
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[@element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-redefined locally") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a" xmlns:tst="http://www.rackspace.com/wadl/test">
                              <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"
                                          xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                          xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                                          xmlns:tst="http://www.rackspace.com/wadl/test/2"/>
                              <rax:foo bla="tst3:c" xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                                                    xmlns:rax="http://docs.rackspace.com/api">
                              </rax:foo>
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[@element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[@element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[@element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined in sep method (local)") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method href="#doPost"/>
               </wadl:resource>
             </wadl:resources>
              <wadl:method id="doPost" name="POST">
                  <wadl:request>
                      <wadl:representation mediaType="application/xml" element="tst:a" xmlns:tst="http://www.rackspace.com/wadl/test">
                           <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"
                                       xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                       xmlns:tst2="http://www.rackspace.com/wadl/test/2"/>
                             <rax:foo bla="tst3:c" xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                                                    xmlns:rax="http://docs.rackspace.com/api">
                              </rax:foo>
                       </wadl:representation>
                  </wadl:request>
              </wadl:method>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined in sep method (in method)") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method href="#doPost"/>
               </wadl:resource>
             </wadl:resources>
              <wadl:method id="doPost" name="POST"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                             xmlns:tst="http://www.rackspace.com/wadl/test"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                             xmlns:rax="http://docs.rackspace.com/api">
                  <wadl:request>
                      <wadl:representation mediaType="application/xml" element="tst:a">
                           <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"/>
                             <rax:foo bla="tst3:c">
                              </rax:foo>
                       </wadl:representation>
                  </wadl:request>
              </wadl:method>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:method[preceding::wadl:resources] and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined in external method (in method)") {
      Given("a WADL with an element definition")
      val inWADL = ("test://path/to/test/my.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method href="other.wadl#doPost"/>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
          register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
                        <wadl:method id="doPost" name="POST"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                             xmlns:tst="http://www.rackspace.com/wadl/test"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                             xmlns:rax="http://docs.rackspace.com/api">
                  <wadl:request>
                      <wadl:representation mediaType="application/xml" element="tst:a">
                           <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"/>
                             <rax:foo bla="tst3:c">
                              </rax:foo>
                       </wadl:representation>
                  </wadl:request>
              </wadl:method>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined in external method (in application)") {
      Given("a WADL with an element definition")
      val inWADL = ("test://path/to/test/my.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b">
                  <wadl:method href="other.wadl#doPost"/>
               </wadl:resource>
             </wadl:resources>
          </wadl:application>)
          register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                             xmlns:tst="http://www.rackspace.com/wadl/test"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                             xmlns:rax="http://docs.rackspace.com/api">
                        <wadl:method id="doPost" name="POST">
                  <wadl:request>
                      <wadl:representation mediaType="application/xml" element="tst:a">
                           <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"/>
                             <rax:foo bla="tst3:c">
                              </rax:foo>
                       </wadl:representation>
                  </wadl:request>
              </wadl:method>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined in resourceType") {
      Given("a WADL with an element definition")
      val inWADL =
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="#doPost">
               </wadl:resource>
             </wadl:resources>
             <wadl:resource_type id="doPost"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                             xmlns:tst="http://www.rackspace.com/wadl/test"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                             xmlns:rax="http://docs.rackspace.com/api">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                              <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"/>
                              <rax:foo bla="tst3:c">
                              </rax:foo>
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resource_type and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined in external resourceType") {
      Given("a WADL with an element definition")
      val inWADL = ("test://path/to/test/my.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="other.wadl#doPost">
               </wadl:resource>
             </wadl:resources>
           </wadl:application>
        )
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02">
             <wadl:resource_type id="doPost"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                             xmlns:tst="http://www.rackspace.com/wadl/test"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                             xmlns:rax="http://docs.rackspace.com/api">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                              <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"/>
                              <rax:foo bla="tst3:c">
                              </rax:foo>
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }

    scenario("The WADL contains a XML representation with an element definition, and plain params. NS re-defined in external resourceType (application)") {
      Given("a WADL with an element definition")
      val inWADL = ("test://path/to/test/my.wadl",
           <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema/foo"
                             xmlns:tst="http://www.rackspace.com/wadl/test/foo"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2/foo"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3/foo"
                             xmlns:rax="http://docs.rackspace.com/api/foo">
             <wadl:grammars>
             </wadl:grammars>
            <wadl:resources base="https://test.api.openstack.com">
               <wadl:resource path="/a/b" type="other.wadl#doPost">
               </wadl:resource>
             </wadl:resources>
           </wadl:application>
        )
        register ("test://path/to/test/other.wadl",
                  <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02" 
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                             xmlns:tst="http://www.rackspace.com/wadl/test"
                             xmlns:tst2="http://www.rackspace.com/wadl/test/2"
                             xmlns:tst3="http://www.rackspace.com/wadl/test/3"
                             xmlns:rax="http://docs.rackspace.com/api">
             <wadl:resource_type id="doPost">
                  <wadl:method name="POST">
                     <wadl:request>
                         <wadl:representation mediaType="application/xml" element="tst:a">
                              <wadl:param style="plain" type="xsd:string" path="/tst:a/tst2:b"/>
                              <rax:foo bla="tst3:c">
                              </rax:foo>
                         </wadl:representation>
                     </wadl:request>
                  </wadl:method>
             </wadl:resource_type>
          </wadl:application>)
      When("the WADL is normalized")
      val normWADL = wadl.normalize(inWADL, TREE, XSD11, false, KEEP)
      Then("The element attribute should remain valid.")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'xsd'")
      assert(normWADL, "namespace-uri-for-prefix('xsd',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.w3.org/2001/XMLSchema'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst'")
      assert(normWADL, "namespace-uri-for-prefix('tst',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'tst2'")
      assert(normWADL, "namespace-uri-for-prefix('tst2',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/wadl:param) = 'http://www.rackspace.com/wadl/test/2'")
      assert(normWADL, "in-scope-prefixes(//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'tst3'")
      assert(normWADL, "namespace-uri-for-prefix('tst3',//wadl:representation[ancestor::wadl:resources and @element='tst:a']/rax:foo) = 'http://www.rackspace.com/wadl/test/3'")
    }
  }
}
