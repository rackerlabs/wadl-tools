package com.rackspace.cloud.api.wadl.test

import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.XSDVersion._
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

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
	       <resource path="/c/d" rax:roles="b:anotherRole"/>
        </resources>
       </application>
       val inWADLNoReferences2 = <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
              <method name="PUT" rax:roles="a:observer"/>
              <resource path="/b" rax:roles="b:creator">
                  <method name="POST"/>
                  <method name="PUT" rax:roles="b:observer"/>
                  <method name="DELETE" rax:roles="b:observer b:admin"/>
              </resource>
           </resource>
          <resource path="/c/d" rax:roles="a:anotherRole" rax:foo="baz"/>
	       <resource path="/c">
             <resource path="d" rax:roles="b:anotherRole" rax:foo="bar"/>
          </resource>
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
	      <resource path="/c/d" rax:roles="b:anotherRole"/>
            </resources>
             <method id="putOnA" name="PUT"/>
             <method id="postOnB" name="POST"/>
             <method id="putOnB" name="PUT" rax:roles="b:observer"/>
             <method id="deleteOnB" name="DELETE" rax:roles="b:foo"/>
           </application>

      When ("When the WADLs are normalized in tree format")
      val normWADLNoRefs = wadl.normalize(inWADLNoReferences, TREE, XSD11, false, KEEP)
      val normWADLNoRefs2 = wadl.normalize(inWADLNoReferences2, TREE, XSD11, false, KEEP)
      val normWADLRefs = wadl.normalize(inWADLWithReferences, TREE, XSD11, false, KEEP)

      Then("XPaths to method references should match")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:method/@rax:roles = 'a:observer'")
      assert (normWADLNoRefs,"not(exists(/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='POST']/@rax:roles))")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='PUT']/@rax:roles = 'b:observer'")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='DELETE']/@rax:roles = 'b:observer b:admin'")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource[@path='d']/@rax:roles = 'a:anotherRole b:anotherRole'")
      assert (normWADLNoRefs,"/wadl:application/wadl:resources/wadl:resource[not(@rax:roles) and @path = 'c']") //Raxroles should not apply to previous resources

      assert (normWADLNoRefs2,"/wadl:application/wadl:resources/wadl:resource/wadl:method/@rax:roles = 'a:observer'")
      assert (normWADLNoRefs2,"not(exists(/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='POST']/@rax:roles))")
      assert (normWADLNoRefs2,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='PUT']/@rax:roles = 'b:observer'")
      assert (normWADLNoRefs2,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='DELETE']/@rax:roles = 'b:observer b:admin'")
      assert (normWADLNoRefs2,"/wadl:application/wadl:resources/wadl:resource/wadl:resource[@path='d']/@rax:roles = 'b:anotherRole a:anotherRole'")
      assert (normWADLNoRefs2,"/wadl:application/wadl:resources/wadl:resource[not(@rax:roles) and @path = 'c']") //Raxroles should not apply to previous resources


      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:method/@rax:roles = 'a:observer'")
      assert (normWADLRefs,"not(exists(/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='POST']/@rax:roles))")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='PUT']/@rax:roles = 'b:observer'")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource/wadl:method[@name='DELETE']/@rax:roles = 'b:observer b:admin'")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource/wadl:resource[@path='d']/@rax:roles = 'a:anotherRole b:anotherRole'")
      assert (normWADLRefs,"/wadl:application/wadl:resources/wadl:resource[not(@rax:roles) and @path = 'c']") //Raxroles should not apply to previous resources
    }

    scenario ("A WADL in Path mode with extensions at various levels.") {
      Given("Given semantically equivilant WADLs with extensions at various levels in PATH mode, with and without references")
      val inWADLNoReferences = <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="test://schema/a">
        <grammars>
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
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin" rax:another="bar">
            <method name="PUT" rax:roles="a:observer">
	      <rax:methodPUTEXT />
            </method>
	    <rax:aFirst part="1">
		<rax:stuff/>
	    </rax:aFirst>
          </resource>
	  <resource path="/a" rax:roles="b:admin" rax:another="foo">
	    <method name="POST">
	       <rax:POST/>
            </method>
	    <rax:aSecond/>
	    <rax:aFirst part="2">
		<rax:moreStuff/>
	    </rax:aFirst>
	  </resource>
	  <resource path="/a/b" rax:roles="b:creator" rax:other="true">
            <method name="POST">
               <rax:POST/>
            </method>
            <method name="PUT" rax:roles="b:observer"/>
            <method name="DELETE" rax:roles="b:observer b:admin"/>
	    <rax:bFirst>
               <rax:otherStuff/>
            </rax:bFirst>
	  </resource>
	  <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
            <rax:yes/>
          </resource>
         <rax:resourcesExt/>
        </resources>
         <rax:appExt/>
      </application>

      val inWADLWithReferences = <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="test://schema/a">
        <grammars>
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
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin" rax:another="bar">
	    <method href="#PUTMethodWithAObserver"/>
	    <rax:aFirst part="1">
		<rax:stuff/>
	    </rax:aFirst>
	  </resource>
	  <resource path="a" rax:roles="b:admin" rax:another="foo">
	    <method href="#POSTMethod"/>
	    <rax:aSecond/>
	    <rax:aFirst part="2">
		<rax:moreStuff/>
	    </rax:aFirst>
	  </resource>
	  <resource path="/a/b" rax:roles="b:creator" rax:other="true">
            <method href="#POSTMethod"/>
	    <method href="#PUTMethodWithBObserver"/>
	    <method href="#DELETEWithBObserverBAdmin"/>
	    <rax:bFirst>
               <rax:otherStuff/>
            </rax:bFirst>
	  </resource>
	  <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method href="#GETMethod"/>
            <rax:yes/>
          </resource>
         <rax:resourcesExt/>
        </resources>
	<method id="PUTMethodWithAObserver" name="PUT" rax:roles="a:observer">
	      <rax:methodPUTEXT />
	</method>
	<method id="POSTMethod" name="POST">
	       <rax:POST/>
	</method>
	<method id="PUTMethodWithBObserver" name="PUT" rax:roles="b:observer"/>
	<method id="DELETEWithBObserverBAdmin" name="DELETE" rax:roles="b:observer b:admin"/>
	<method id="GETMethod" name="GET"/>
      <rax:appExt/>
      </application>

      When ("When the WADLs are normalized in tree format")
      val normWADLNoRefs = wadl.normalize(inWADLNoReferences, TREE, XSD11, false, KEEP)
      val normWADLRefs = wadl.normalize(inWADLWithReferences, TREE, XSD11, false, KEEP)

      val normWADLS = List(normWADLNoRefs, normWADLRefs)

      Then("XPaths to method reforences should match")
      normWADLS.foreach (w => {
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/@rax:roles='a:admin b:admin'")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/@rax:another=('foo','bar')") // Could be foo or bar it's nondeterministic
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:method[@name='PUT']/@rax:roles='a:observer'")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:method[@name='PUT']/rax:methodPUTEXT")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/rax:aFirst[@part='1']/rax:stuff")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/rax:aFirst[@part='2']/rax:moreStuff")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/rax:aSecond")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:method[@name='POST' and not(@rax:roles)]")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:method[@name='POST' and not(@rax:roles)]/rax:POST")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/@rax:roles='b:creator'")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/rax:bFirst/rax:otherStuff")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='POST' and not(@rax:roles)]")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='POST' and not(@rax:roles)]/rax:POST")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='PUT']/@rax:roles = 'b:observer'")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='DELETE']/@rax:roles = 'b:observer b:admin'")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='{yn}']/@rax:roles='a:admin'")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='{yn}']/wadl:method[@name='GET' and not(@rax:roles)]")
	assert(w, "/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='{yn}']/rax:yes")
   assert(w, "count(/wadl:application/rax:appExt) = 1")
   assert(w, "count(/wadl:application/wadl:resources/rax:resourcesExt) = 1")
      })
    }

    scenario ("A WADL in a very Mixed Tree/Path mode with extensions at various levels.") {
      Given("Given semantically equivilant WADLs with extensions at various levels in Tree and Path mode, with and without references")
      val inWADLNoReferences = <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="test://schema/a">
        <grammars>
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
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="a" rax:roles="and_another:admin">
            <method name="GET" rax:roles="a:observer"/>
          </resource>
	  <resource path="/a" rax:roles="a:admin another:admin">
	    <method name="PUT" rax:roles="a:observer"/>
	    <resource path="b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
	      <resource path="c" rax:roles="d:creator">
		<method name="GET"/>
		<resource path="g" rax:roles="foo:bar">
		  <method name="GET"/>
		</resource>
	      </resource>
	    </resource>
          </resource>
	  <resource path="/a/b/c" rax:roles="c:creator">
            <method name="POST"/>
	  </resource>
	  <resource path="/a/b/c/g" rax:roles="ttns:creator">
            <method name="POST"/>
	  </resource>
	  <resource path="/a/b/c/d/e" rax:roles="c:creator">
            <method name="POST"/>
	  </resource>
	  <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
	  <resource path="/a/b/c" rax:roles="f:creator">
	    <method name="DELETE"/>
	    <resource path="g" rax:roles="e:creator">
	      <method name="DELETE"/>
	    </resource>
	  </resource>
         <rax:resourcesExt/>
        </resources>
         <rax:appExt/>
      </application>

      val inWADLNoReferences2 = <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="test://schema/a">
        <grammars>
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
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="a" rax:roles="and_another:admin">
            <method name="GET" rax:roles="a:observer"/>
          </resource>
	  <resource path="/a" rax:roles="a:admin another:admin">
	    <method name="PUT" rax:roles="a:observer"/>
	    <resource path="b" rax:roles="b:creator">
	      <resource path="c" rax:roles="d:creator">
		<method name="GET"/>
		<resource path="g" rax:roles="foo:bar">
		  <method name="GET"/>
		</resource>
	      </resource>
	    </resource>
          </resource>
          <resource path="/a/b">
            <method name="POST"/>
            <method name="PUT" rax:roles="b:observer"/>
            <method name="DELETE" rax:roles="b:observer b:admin"/>
	    <resource path="c" rax:roles="f:creator">
	      <method name="DELETE"/>
	        <resource path="g" rax:roles="e:creator">
	         <method name="DELETE"/>
	        </resource>
	    </resource>
	  </resource>
	  <resource path="/a/b/c" rax:roles="c:creator">
            <method name="POST"/>
	  </resource>
	  <resource path="/a/b/c/g" rax:roles="ttns:creator">
            <method name="POST"/>
	  </resource>
	  <resource path="/a/b/c/d/e" rax:roles="c:creator">
            <method name="POST"/>
	  </resource>
	  <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
         <rax:resourcesExt/>
        </resources>
         <rax:appExt/>
      </application>

      val inWADLWithReferences = <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="test://schema/a">
        <grammars>
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
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="a" rax:roles="and_another:admin">
            <method href="#GETObserver"/>
          </resource>
	  <resource path="/a" rax:roles="a:admin another:admin">
	    <method href="#PUTObserver"/>
	    <resource path="b" rax:roles="b:creator">
              <method href="#POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
	      <resource path="c" rax:roles="d:creator">
		<method name="GET"/>
		<resource path="g" rax:roles="foo:bar">
		  <method name="GET"/>
		</resource>
	      </resource>
	    </resource>
          </resource>
	  <resource path="/a/b/c" rax:roles="c:creator">
            <method href="#POST"/>
	  </resource>
	  <resource path="/a/b/c/g" rax:roles="ttns:creator">
            <method href="#POST"/>
	  </resource>
	  <resource path="/a/b/c/d/e" rax:roles="c:creator">
            <method href="#POST"/>
	  </resource>
	  <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
	  <resource path="/a/b/c" rax:roles="f:creator">
	    <method name="DELETE"/>
	    <resource path="g" rax:roles="e:creator">
	      <method name="DELETE"/>
	    </resource>
	  </resource>
         <rax:resourcesExt/>
        </resources>
        <method id="PUTObserver" name="PUT" rax:roles="a:observer"/>
        <method id="GETObserver" name="GET" rax:roles="a:observer"/>
        <method id="POST" name="POST"/>
         <rax:appExt/>
      </application>

      When ("When the WADLs are normalized in tree format")
      val normWADLNoRefs = wadl.normalize(inWADLNoReferences, TREE, XSD11, false, KEEP)
      val normWADLNoRefs2 = wadl.normalize(inWADLNoReferences2, TREE, XSD11, false, KEEP)
      val normWADLRefs = wadl.normalize(inWADLWithReferences, TREE, XSD11, false, KEEP)


      val normWADLS = List(normWADLNoRefs, normWADLNoRefs2, normWADLRefs)

      Then("XPaths to method reforences should match")
      normWADLS.foreach (w => {
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/@rax:roles='a:admin another:admin and_another:admin'")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:method[@name='GET']/@rax:roles='a:observer'")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:method[@name='PUT']/@rax:roles='a:observer'")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='{yn}']/@rax:roles='a:admin'")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='{yn}']/wadl:param[@name='yn' and @style='template' and @type='tst:yesno']")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='{yn}']/wadl:method[@name='GET' and not(@rax:roles)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/@rax:roles='b:creator'")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='POST' and not(@rax:roles)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='PUT']/@rax:roles='b:observer'")
	assert (w,"every $r in tokenize(/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:method[@name='DELETE']/@rax:roles,' ') satisfies $r = ('b:observer', 'b:admin')")
	assert (w,"every $r in tokenize(/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/@rax:roles,' ') satisfies $r = ('d:creator', 'c:creator', 'f:creator')")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:method[@name='POST' and not(@rax:roles)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:method[@name='DELETE' and not(@rax:roles)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:method[@name='GET' and not(@rax:roles)]")
	assert (w,"every $r in tokenize(/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='g']/@rax:roles, ' ') satisfies $r = ('foo:bar', 'e:creator', 'ttns:creator')")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='g']/wadl:method[@name='GET' and not(@rax:roles)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='g']/wadl:method[@name='POST' and not(@rax:roles)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='g']/wadl:method[@name='DELETE' and not(@rax:roles)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='d' and not(@rax:roles) and not(wadl:method)]")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='d']/wadl:resource[@path='e']/@rax:roles='c:creator'")
	assert (w,"/wadl:application/wadl:resources/wadl:resource[@path='a']/wadl:resource[@path='b']/wadl:resource[@path='c']/wadl:resource[@path='d']/wadl:resource[@path='e']/wadl:method[@name='POST' and not(@rax:roles)]")
   assert(w, "count(/wadl:application/rax:appExt) = 1")
   assert(w, "count(/wadl:application/wadl:resources/rax:resourcesExt) = 1")

      })
    }
  }

}
