package com.sksamuel.elastic4s.analyzers

import org.elasticsearch.common.xcontent.{XContentBuilder, XContentFactory}

import scala.collection.JavaConverters._

// Base class for normalizers that have custom parameters set.
abstract class NormalizerDefinition (val name: String) {

  def buildWithName(source: XContentBuilder): Unit = {
    source.startObject(name)
    build(source)
    source.endObject()
  }

  def buildWithName(): XContentBuilder = {
    val xc = XContentFactory.jsonBuilder()
    xc.startObject()
    buildWithName(xc)
    xc.endObject()
    xc
  }

  def build(): XContentBuilder = {
    val xc = XContentFactory.jsonBuilder()
    xc.startObject()
    build(xc)
    xc.endObject()
    xc
  }

  def build(source: XContentBuilder): Unit

  def json: XContentBuilder = {
    val builder = XContentFactory.jsonBuilder
    builder.startObject()
    build(builder)
    builder.endObject()
    builder
  }
}


case class CustomNormalizerDefinition(override val name: String,
                                    filters: Seq[AnalyzerFilter] = Nil) extends NormalizerDefinition(name) {

  def build(source: XContentBuilder): Unit = {
    source.field("type", "custom")
    val tokenFilters = filters.collect { case token: TokenFilter => token }
    val charFilters = filters.collect { case char: CharFilter => char }
    if (tokenFilters.nonEmpty) {
      source.field("filter", tokenFilters.map(_.name).asJava)
    }
    if (charFilters.nonEmpty) {
      source.field("char_filter", charFilters.map(_.name).asJava)
    }
  }

  def filters(filters: Seq[AnalyzerFilter]): CustomNormalizerDefinition = copy(filters = filters)
  def addFilter(filter: AnalyzerFilter): CustomNormalizerDefinition = copy(filters = filters :+ filter)
}

object CustomNormalizerDefinition {
  def apply(name: String,
            first: AnalyzerFilter,
            rest: AnalyzerFilter*): CustomNormalizerDefinition = {
    CustomNormalizerDefinition(name, first +: rest)
  }
}
