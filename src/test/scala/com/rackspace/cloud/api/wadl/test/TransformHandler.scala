package com.rackspace.cloud.api.wadl.test

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URI

import com.rackspace.cloud.api.wadl.Converters._
import javax.xml.transform.stream.{StreamResult, StreamSource}
import javax.xml.transform.{Result, Source, TransformerFactory, URIResolver}
import net.sf.saxon.lib.OutputURIResolver

import scala.collection.mutable.{HashMap, Map}
import scala.xml.NodeSeq

trait TransformHandler {
  val transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass.getClassLoader)

  private val defaultResolver = transformerFactory.getURIResolver
  private val sourceMap : Map[String, Source] = new HashMap[String, Source]()
  private val destMap : Map[String, StreamResult] = new HashMap[String, StreamResult]()

  //
  //  Set input URL resolver
  //
  transformerFactory.setURIResolver (new Object() with URIResolver {
    //
    //  URL resolver implementation, I'm ignoring the base input, I
    //  don't think that we need it.
    //
    def resolve(href : String, base : String) = {
      val ref = {
        if (base == null || base.isEmpty()) {
          href
        } else {
          val baseURI = new URI(base)
          baseURI.resolve(href).toString()
        }
      }
      val source = sourceMap getOrElse (ref, defaultResolver.resolve(href, base))
      if (source.isInstanceOf[StreamSource]) {
        source.asInstanceOf[StreamSource].getInputStream().reset()
      }
      source
    }
  })

  //
  //  Set output URL resolver
  //
  transformerFactory.setAttribute ("http://saxon.sf.net/feature/outputURIResolver", new Object() with OutputURIResolver {
    //
    //  Output URI resolver, again ignoring the base here...
    //
    def resolve(href : String, base : String) = {
      val result = new StreamResult(new ByteArrayOutputStream())
      destMap += (href -> result)
      result
    }

    //
    //  Close the result
    //
    def close(result : Result) = {
      result.asInstanceOf[StreamResult].getOutputStream().close()
    }

    //
    //  Return a new instance
    //
    def newInstance = this
  })

  //
  //  Add a source to consider
  //
  def register (url : String, xml : NodeSeq) : Unit = {
    val conv : (String, ByteArrayInputStream) = (url, xml);
    val streamSource : StreamSource = new StreamSource (conv._2)
    streamSource.setSystemId(conv._1)
    sourceMap += (url -> streamSource)
  }

  def register (in : (String, NodeSeq)) : Unit = register(in._1, in._2)

  //
  //  Get outputs
  //
  def outputs : Map[String, NodeSeq] = {
    val result : Map[String, NodeSeq] = new HashMap[String, NodeSeq]()
    destMap foreach ( (t) => result += (t._1 -> t._2))
    result
  }

  //
  //  Clear all data...
  //
  def reset : Unit = {
    destMap.clear()
    sourceMap.clear()
  }
}
