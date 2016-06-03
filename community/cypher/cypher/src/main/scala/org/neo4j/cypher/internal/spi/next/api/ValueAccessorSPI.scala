package org.neo4j.cypher.internal.spi.next.api

import org.neo4j.cypher.internal.frontend.v3_1.symbols.CypherType

trait ValueAccessorSPI {
  self: KernelTypeSPI =>

  // Notion of how to read/write values from/to a location of type L
  trait ValueAccessor[L] {
    def cypherType: CypherType

    def getNodeFrom(source: L): self.Node
    def getIntegerAsLongFrom(source: L): Long
    def getValueAsTextFrom(source: L): String

    def putNodeTo(value: self.Node, sink: L)
    def putLongAsInteger(value: Long, sink: L)
  }
}
