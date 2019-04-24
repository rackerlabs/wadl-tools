package com.rackspace.cloud.api.wadl

import java.io.ByteArrayInputStream

import com.rackspace.cloud.api.wadl.util.LogErrorListener
import javax.xml.transform.stream.StreamResult
import net.sf.saxon.jaxp.TransformerImpl
import net.sf.saxon.serialize.MessageWarner
import org.w3c.dom.{Node, NodeList}

import scala.xml.{NodeSeq, XML}

//
//  Converters
//
object Converters {
  //
  //  Convert a node sequence to a ByteArrayInputStream
  //
  implicit def nodeSeq2ByteArrayInputStream(ns : NodeSeq) : ByteArrayInputStream = new ByteArrayInputStream(ns.toString().getBytes())


  //
  //  Convert a node sequence string touple to a ByteArrayInputStream with a system ID set
  //
  implicit def nodeSeqString2Source (nss : (String, NodeSeq)) : (String, ByteArrayInputStream) = {
    val s = nodeSeq2ByteArrayInputStream(nss._2)
    (nss._1, s)
  }

  //
  //  Convert a byte array stream result to a NodeSeq
  //
  implicit def byteArrayStreamResult2NodeSeq(sr : StreamResult) : NodeSeq = XML.loadString (sr.getOutputStream().toString())


  //
  //  Adds log error listener to a Saxon controller
  //
  implicit def toLogController(t : TransformerImpl) = new {
    def addLogErrorListener : Unit = {
      t.setErrorListener (new LogErrorListener)
      t.getUnderlyingController.setMessageEmitter(new MessageWarner())
    }
  }

  implicit def toList(nl : NodeList) : List[Node] = {
    def nodeList(nl : NodeList, curr : List[Node], i : Int) : List[Node] = {
      if (i < nl.getLength()) {
        nodeList(nl, curr ::: List(nl.item(i)), i+1)
      } else {
        curr
      }
    }

    nodeList(nl, List[Node](), 0)
  }
}
