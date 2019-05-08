package com.rackspace.cloud.api.wadl.util

import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

import scala.collection.mutable.HashSet

class EntityCatcher extends DefaultHandler {
  val systemIds  : HashSet[String] = HashSet()

  override def resolveEntity (publicId : String, systemId : String) : InputSource = {
    systemIds += systemId
    super.resolveEntity(publicId, systemId)
  }
}
