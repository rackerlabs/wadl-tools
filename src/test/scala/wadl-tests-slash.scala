package com.rackspace.cloud.api.wadl.test

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.Converters._

@RunWith(classOf[JUnitRunner])
class NormalizeWADLSlashSpec extends BaseWADLSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("rax", "http://docs.rackspace.com/api")

  feature ("The WADL normalizer can convert resources in a tree format and ignore extra slashes //") {

    info("As a developer")
    info("I want to ignore extra slashes in the WADL cus they happen all the time and they are irrelevent")
    info("So that I can normalize a WADL without worrying about cleaning extra slashes up.")

    val testWadls : List[(String, NodeSeq, Option[(String, NodeSeq)])] =
      List(
        ("No double slashes",
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
          <resource path="/">
             <method name="GET"/>
          </resource>
          <resource path="/">
             <method name="POST"/>
               <resource path="/a">
                 <method name="PUT"/>
                   <resource path="/b">
                       <method name="POST"/>
                       <method name="PUT"/>
                       <method name="DELETE"/>
                   </resource>
               </resource>
          </resource>
          <resource path="/a/b/c/d">
	    <method name="GET"/>
	  </resource>
	  <resource path="/foo">
	      <method name="GET"/>
	      <resource path="bar">
		  <method name="POST"/>
	      </resource>
	  </resource>
        </resources>
       </application>, None),
       ("No double slashes : root defined elsewhere",
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
        <resource path="/a">
          <method name="PUT"/>
          <resource path="/b">
              <method name="POST"/>
              <method name="PUT"/>
              <method name="DELETE"/>
          </resource>
         </resource>
          <resource path="/a/b/c/d">
	    <method name="GET"/>
	  </resource>
          <resource path="/">
            <method name="GET"/>
            <method name="POST"/>
	    <resource path="/foo">
	      <method name="GET"/>
	      <resource path="bar">
		  <method name="POST"/>
	      </resource>
	    </resource>
          </resource>
        </resources>
       </application>, None),
        ("No double slashes root split",
         <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
          <resource path="/">
             <method name="GET"/>
          </resource>
          <resource path="/a">
              <method name="PUT"/>
              <resource path="/b">
                  <method name="POST"/>
                  <method name="PUT"/>
                  <method name="DELETE"/>
              </resource>
           </resource>
          <resource path="/a/b/c/d">
	    <method name="GET"/>
	  </resource>
	  <resource path="/foo">
	      <method name="GET"/>
	      <resource path="bar">
		  <method name="POST"/>
	      </resource>
	  </resource>
          <resource path="/">
             <method name="POST"/>
          </resource>
        </resources>
       </application>, None),
        ("No double slashes root split (2)",
         <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
          <resource path="/">
             <method name="GET"/>
          </resource>
          <resource path="/a">
              <method name="PUT"/>
              <resource path="/b">
                  <method name="POST"/>
                  <method name="PUT"/>
                  <method name="DELETE"/>
              </resource>
           </resource>
          <resource path="/a/b/c/d">
	    <method name="GET"/>
	  </resource>
          <resource path="/">
             <method name="POST"/>
	  <resource path="/foo">
	      <method name="GET"/>
	      <resource path="bar">
		  <method name="POST"/>
	      </resource>
	  </resource>
          </resource>
        </resources>
       </application>, None),
        ("Double slash in the root",
         <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
          <resource path="//">
             <method name="GET"/>
             <method name="POST"/>
          </resource>
          <resource path="///a">
              <method name="PUT"/>
              <resource path="/b">
                  <method name="POST"/>
                  <method name="PUT"/>
                  <method name="DELETE"/>
              </resource>
           </resource>
          <resource path="/a/b/c/d">
	    <method name="GET"/>
	  </resource>
	  <resource path="/foo">
	      <method name="GET"/>
	      <resource path="bar">
		  <method name="POST"/>
	      </resource>
	  </resource>
        </resources>
       </application>, None),
       ("Double slashes at the end of the path",
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
          <resource path="/">
             <method name="GET"/>
             <method name="POST"/>
          </resource>
          <resource path="/a">
              <method name="PUT"/>
              <resource path="/b//">
                  <method name="POST"/>
                  <method name="PUT"/>
                  <method name="DELETE"/>
              </resource>
           </resource>
          <resource path="/a/b/c/d//">
	    <method name="GET"/>
	  </resource>
	  <resource path="/foo">
	      <method name="GET"/>
	      <resource path="bar//">
		  <method name="POST"/>
	      </resource>
	  </resource>
        </resources>
       </application>, None),
       ("Double slashes in various parts of the path",
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
          <resource path="/">
             <method name="GET"/>
             <method name="POST"/>
          </resource>
          <resource path="/a">
              <method name="PUT"/>
              <resource path="//b">
                  <method name="POST"/>
                  <method name="PUT"/>
                  <method name="DELETE"/>
              </resource>
           </resource>
          <resource path="/a/b/c///d">
	    <method name="GET"/>
	  </resource>
	  <resource path="//foo">
	      <method name="GET"/>
	      <resource path="bar">
		  <method name="POST"/>
	      </resource>
	  </resource>
        </resources>
       </application>, None),
       ("Double slashes in various parts of the path, with method references.",
        <application xmlns="http://wadl.dev.java.net/2009/02">
            <resources base="https://test.api.openstack.com">
                <resource path="/">
                    <method name="GET"/>
                    <method name="POST"/>
                </resource>
               <resource path="/a">
                 <method href="#putOnA"/>
                   <resource path="//b">
                     <method href="#postOnB"/>
                     <method href="#putOnB"/>
                     <method href="#deleteOnB"/>
                   </resource>
               </resource>
              <resource path="/a/b/c///d">
		 <method href="#getOnD"/>
	      </resource>
	      <resource path="//foo">
		<method href="#getOnD"/>
		<resource path="bar">
		  <method href="#postOnB"/>
		</resource>
	      </resource>
            </resources>
             <method id="putOnA" name="PUT"/>
             <method id="postOnB" name="POST"/>
             <method id="putOnB" name="PUT"/>
             <method id="deleteOnB" name="DELETE"/>
             <method id="getOnD" name="GET"/>
           </application>, None),
       ("Double slashes in external file",
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <resources base="https://test.api.openstack.com">
          <resource path="/">
             <method name="GET"/>
             <method name="POST"/>
          </resource>
          <resource path="/a">
              <method name="PUT"/>
              <resource path="/b" type="other.wadl#cd">
                  <method name="POST"/>
                  <method name="PUT"/>
                  <method name="DELETE"/>
              </resource>
           </resource>
	  <resource path="/foo">
	      <method name="GET"/>
	      <resource path="bar">
		  <method name="POST"/>
	      </resource>
	  </resource>
        </resources>
       </application>, Some(("test://path/to/test/other.wadl",
                             <application xmlns="http://wadl.dev.java.net/2009/02">
                             <resource_type id="cd">
		             <resource path="c///d">
			     <method name="GET"/>
		             </resource>
                             </resource_type>
                             </application>)))
      )

    testWadls.foreach (tw => {
      val desc   = tw._1
      val inWADL = tw._2
      val ext    = tw._3

      scenario ("Extra slashes test: "+desc) {
        Given("Given semantically equivilant WADLs with and without extra slashes: "+desc)

        ext match {
          case Some((name, ext)) => register(name, ext)
          case None => /* ignore */
        }

        val w = wadl.normalize(("test://path/to/test/mywadl.wadl", inWADL), TREE, XSD11, false, KEEP)

        When ("When the WADLs are normalized in tree format")
        Then("The following XPath assertions should match")
	//
	//  Overall structure
	//
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:resource[@path='b']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='d']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='foo']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='foo']/wadl:resource[@path='bar']")

	//
	//  Methods
	//
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:method[@name='POST']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:method[@name='GET']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:method[@name='PUT']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='POST']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='PUT']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='DELETE']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='d']/wadl:method[@name='GET']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='foo']/wadl:method[@name='GET']")
	assert (w, "/wadl:application/wadl:resources/wadl:resource[@path='/']/wadl:resource[@path='foo']/wadl:resource[@path='bar']/wadl:method[@name='POST']")
      }
    })
  }
}
