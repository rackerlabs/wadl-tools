package com.rackspace.cloud.api.wadl.test

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.ShouldMatchers._

import WADLFormat._
import XSDVersion._

@RunWith(classOf[JUnitRunner])
class NormalizeWADLSpec extends BaseWADLSpec with GivenWhenThen {

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
              </resources>
        </application>
      when("the WADL is normalized")
      val normWADL = normalizeWADL(inWADL, TREE)
      then("the resources should remain unchanged")
      conon(inWADL) should equal (conon(normWADL))
    }

    scenario ("The original WADL is in the path format") (pending)
    scenario ("The original WADL is in mixed path/tree format") (pending)
  }
}
