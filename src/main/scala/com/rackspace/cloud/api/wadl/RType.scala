package com.rackspace.cloud.api.wadl

object RType extends Enumeration {
  type ResourceType = Value
  val KEEP = Value("keep")
  val OMIT = Value("omit")
}
