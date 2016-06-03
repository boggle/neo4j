package org.neo4j.cypher.internal.spi.next.impl

import org.neo4j.cypher.internal.frontend.v3_1.symbols.CypherType
import org.neo4j.cypher.internal.spi.next.api.{KernelTypeSPI, ValueAccessorSPI}

import scala.collection.mutable

trait DynamicValueAccessor {
  self: KernelTypeSPI with ValueAccessorSPI =>

  class DynamicValueAccessor(val cypherType: CypherType,
                             val column: String) extends ValueAccessor[mutable.Map[String,Any]] {
    def getNodeFrom(source: mutable.Map[String,Any]) = anyToNode(source(column))
    def getIntegerAsLongFrom(source: mutable.Map[String,Any]): Long = source(column).asInstanceOf[Number].longValue()
    def getValueAsTextFrom(source: mutable.Map[String,Any]): String = source(column).toString

    def putNodeTo(value: self.Node, sink: mutable.Map[String,Any]) = {
      sink += column -> nodeToAny(value)
    }

    def putLongAsInteger(value: Long, sink: mutable.Map[String, Any]): Unit = {
      sink += column -> value
    }
  }

  def anyToNode(v: Any): self.Node
  def nodeToAny(v: self.Node): Any
}
