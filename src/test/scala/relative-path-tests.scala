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
class RelativePathSpec extends BaseWADLSpec {

  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("rax", "http://docs.rackspace.com/api")
  register ("xsl", "http://www.w3.org/1999/XSL/Transform")

  feature ("The WADL normalizer should convert relative paths to absolute paths when normalizing the WADL") {

    info("As a developer")
    info("I want to make sure that absolute paths are converted to relative paths when the WADL is normalized")

    scenario ("A WADL containing various HREFS is converted to tree format") {
      given("A WADL with various hrefs")
      val inWADL = ("test://path/to/test/mywadl.wadl",
          <application xmlns="http://wadl.dev.java.net/2009/02"
                       xmlns:rax="http://docs.rackspace.com/api"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema">
              <grammars>
                  <include href="xsd/schema1.xsd"/>
              </grammars>
              <resources base="http://localhost/">
                  <resource path="info">
                      <method id="addAtomHopperEntry" name="POST">
                          <request>
                              <representation mediaType="application/atom+xml">
                                  <rax:preprocess href="xsl/preproc.xsl"/>
                              </representation>
                          </request>
                      </method>
                  </resource>
              </resources>
          </application>)
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsl/preproc.xsl",
                <xsl:stylesheet
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    version="1.0">

                    <xsl:output method="xml" encoding="UTF-8"/>

                    <xsl:template match="node() | @*">
                        <xsl:copy>
                            <xsl:apply-templates select="@* | node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>)
      when("When the wadl is normalized...")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, false, KEEP)
      then("The relative paths should convert to absolute paths")
      assert(normWADL, "wadl:application/wadl:grammars/wadl:include/@href = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/@href = 'test://path/to/test/xsl/preproc.xsl'")
    }

    scenario ("A WADL containing various HREFS is converted to path format") {
      given("A WADL with various hrefs")
      val inWADL = ("test://path/to/test/mywadl.wadl",
          <application xmlns="http://wadl.dev.java.net/2009/02"
                       xmlns:rax="http://docs.rackspace.com/api"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema">
              <grammars>
                  <include href="xsd/schema1.xsd"/>
              </grammars>
              <resources base="http://localhost/">
                  <resource path="info">
                      <method id="addAtomHopperEntry" name="POST">
                          <request>
                              <representation mediaType="application/atom+xml">
                                  <rax:preprocess href="xsl/preproc.xsl"/>
                              </representation>
                          </request>
                      </method>
                  </resource>
              </resources>
          </application>)
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsl/preproc.xsl",
                <xsl:stylesheet
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    version="1.0">

                    <xsl:output method="xml" encoding="UTF-8"/>

                    <xsl:template match="node() | @*">
                        <xsl:copy>
                            <xsl:apply-templates select="@* | node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>)
      when("When the wadl is normalized...")
      val normWADL = wadl.normalize(inWADL, PATH, XSD10, false, KEEP)
      then("The relative paths should convert to absolute paths")
      assert(normWADL, "wadl:application/wadl:grammars/wadl:include/@href = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/@href = 'test://path/to/test/xsl/preproc.xsl'")
    }

    scenario ("A WADL containing various HREFS is converted to dont format") {
      given("A WADL with various hrefs")
      val inWADL = ("test://path/to/test/mywadl.wadl",
          <application xmlns="http://wadl.dev.java.net/2009/02"
                       xmlns:rax="http://docs.rackspace.com/api"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema">
              <grammars>
                  <include href="xsd/schema1.xsd"/>
              </grammars>
              <resources base="http://localhost/">
                  <resource path="info">
                      <method id="addAtomHopperEntry" name="POST">
                          <request>
                              <representation mediaType="application/atom+xml">
                                  <rax:preprocess href="xsl/preproc.xsl"/>
                              </representation>
                          </request>
                      </method>
                  </resource>
              </resources>
          </application>)
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsl/preproc.xsl",
                <xsl:stylesheet
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    version="1.0">

                    <xsl:output method="xml" encoding="UTF-8"/>

                    <xsl:template match="node() | @*">
                        <xsl:copy>
                            <xsl:apply-templates select="@* | node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>)
      when("When the wadl is normalized...")
      val normWADL = wadl.normalize(inWADL, DONT, XSD10, false, KEEP)
      then("The relative paths should convert to absolute paths")
      assert(normWADL, "wadl:application/wadl:grammars/wadl:include/@href = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/@href = 'test://path/to/test/xsl/preproc.xsl'")
    }

    scenario ("A WADL containing emdeded XML with abolute paths is converted to tree format") {
      given("A WADL with various hrefs")
      val inWADL = ("test://path/to/test/mywadl.wadl",
          <application xmlns="http://wadl.dev.java.net/2009/02"
                       xmlns:rax="http://docs.rackspace.com/api"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema">
              <grammars>
                    <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                      <include schemaLocation="xsd/schema1.xsd"/>
                      <import namespace="test://schema/a" schemaLocation="xsd/schema1.xsd"/>
                   </schema>
              </grammars>
              <resources base="http://localhost/">
                  <resource path="info">
                      <method id="addAtomHopperEntry" name="POST">
                          <request>
                              <representation mediaType="application/atom+xml">
                                  <rax:preprocess>
                                     <xsl:stylesheet
                                           xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                           version="2.0">
                                         <xsl:import-schema namespace="test://schema/a" schemaLocation="xsd/schema1.xsd"/>
                                         <xsl:import href="xsl/preproc.xsl"/>
                                         <xsl:include href="xsl/preproc.xsl"/>
                                         <xsl:result-document href="out.txt" method="text">
                                              <xsl:text>Hello?</xsl:text>
                                         </xsl:result-document>
                                     </xsl:stylesheet>
                                  </rax:preprocess>
                              </representation>
                          </request>
                      </method>
                  </resource>
              </resources>
          </application>)
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsl/preproc.xsl",
                <xsl:stylesheet
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    version="1.0">

                    <xsl:output method="xml" encoding="UTF-8"/>

                    <xsl:template match="node() | @*">
                        <xsl:copy>
                            <xsl:apply-templates select="@* | node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>)
      when("When the wadl is normalized...")
      val normWADL = wadl.normalize(inWADL, TREE, XSD10, false, KEEP)
      then("The relative paths should convert to absolute paths")
      assert(normWADL, "wadl:application/wadl:grammars/xsd:schema/xsd:include/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL, "wadl:application/wadl:grammars/xsd:schema/xsd:import/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:import-schema/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:import/@href = 'test://path/to/test/xsl/preproc.xsl'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:include/@href = 'test://path/to/test/xsl/preproc.xsl'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:result-document/@href = 'test://path/to/test/out.txt'")
    }

    scenario ("A WADL containing emdeded XML with abolute paths is converted to path format") {
      given("A WADL with various hrefs")
      val inWADL = ("test://path/to/test/mywadl.wadl",
          <application xmlns="http://wadl.dev.java.net/2009/02"
                       xmlns:rax="http://docs.rackspace.com/api"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema">
              <grammars>
                    <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                      <include schemaLocation="xsd/schema1.xsd"/>
                      <import namespace="test://schema/a" schemaLocation="xsd/schema1.xsd"/>
                   </schema>
              </grammars>
              <resources base="http://localhost/">
                  <resource path="info">
                      <method id="addAtomHopperEntry" name="POST">
                          <request>
                              <representation mediaType="application/atom+xml">
                                  <rax:preprocess>
                                     <xsl:stylesheet
                                           xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                           version="2.0">
                                         <xsl:import-schema namespace="test://schema/a" schemaLocation="xsd/schema1.xsd"/>
                                         <xsl:import href="xsl/preproc.xsl"/>
                                         <xsl:include href="xsl/preproc.xsl"/>
                                         <xsl:result-document href="out.txt" method="text">
                                              <xsl:text>Hello?</xsl:text>
                                         </xsl:result-document>
                                     </xsl:stylesheet>
                                  </rax:preprocess>
                              </representation>
                          </request>
                      </method>
                  </resource>
              </resources>
          </application>)
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsl/preproc.xsl",
                <xsl:stylesheet
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    version="1.0">

                    <xsl:output method="xml" encoding="UTF-8"/>

                    <xsl:template match="node() | @*">
                        <xsl:copy>
                            <xsl:apply-templates select="@* | node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>)
      when("When the wadl is normalized...")
      val normWADL = wadl.normalize(inWADL, PATH, XSD10, false, KEEP)
      then("The relative paths should convert to absolute paths")
      assert(normWADL, "wadl:application/wadl:grammars/xsd:schema/xsd:include/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL, "wadl:application/wadl:grammars/xsd:schema/xsd:import/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:import-schema/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:import/@href = 'test://path/to/test/xsl/preproc.xsl'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:include/@href = 'test://path/to/test/xsl/preproc.xsl'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:result-document/@href = 'test://path/to/test/out.txt'")
    }

    scenario ("A WADL containing emdeded XML with abolute paths is converted to dont format") {
      given("A WADL with various hrefs")
      val inWADL = ("test://path/to/test/mywadl.wadl",
          <application xmlns="http://wadl.dev.java.net/2009/02"
                       xmlns:rax="http://docs.rackspace.com/api"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema">
              <grammars>
                    <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                      <include schemaLocation="xsd/schema1.xsd"/>
                      <import namespace="test://schema/a" schemaLocation="xsd/schema1.xsd"/>
                   </schema>
              </grammars>
              <resources base="http://localhost/">
                  <resource path="info">
                      <method id="addAtomHopperEntry" name="POST">
                          <request>
                              <representation mediaType="application/atom+xml">
                                  <rax:preprocess>
                                     <xsl:stylesheet
                                           xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                           version="2.0">
                                         <xsl:import-schema namespace="test://schema/a" schemaLocation="xsd/schema1.xsd"/>
                                         <xsl:import href="xsl/preproc.xsl"/>
                                         <xsl:include href="xsl/preproc.xsl"/>
                                         <xsl:result-document href="out.txt" method="text">
                                              <xsl:text>Hello?</xsl:text>
                                         </xsl:result-document>
                                     </xsl:stylesheet>
                                  </rax:preprocess>
                              </representation>
                          </request>
                      </method>
                  </resource>
              </resources>
          </application>)
      register ("test://path/to/test/xsd/schema1.xsd",
                <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
                        targetNamespace="test://schema/a">
                    <element vc:minVersion="1.0" vc:maxVersion="1.1" name="test" type="xsd:string"/>
                </schema>)
      register ("test://path/to/test/xsl/preproc.xsl",
                <xsl:stylesheet
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    version="1.0">

                    <xsl:output method="xml" encoding="UTF-8"/>

                    <xsl:template match="node() | @*">
                        <xsl:copy>
                            <xsl:apply-templates select="@* | node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>)
      when("When the wadl is normalized...")
      val normWADL = wadl.normalize(inWADL, DONT, XSD10, false, KEEP)
      then("The relative paths should convert to absolute paths")
      assert(normWADL, "wadl:application/wadl:grammars/xsd:schema/xsd:include/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL, "wadl:application/wadl:grammars/xsd:schema/xsd:import/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:import-schema/@schemaLocation = 'test://path/to/test/xsd/schema1.xsd'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:import/@href = 'test://path/to/test/xsl/preproc.xsl'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:include/@href = 'test://path/to/test/xsl/preproc.xsl'")
      assert(normWADL,
             "wadl:application/wadl:resources/wadl:resource/wadl:method/wadl:request/wadl:representation/rax:preprocess/xsl:stylesheet/xsl:result-document/@href = 'test://path/to/test/out.txt'")
    }
  }
}
