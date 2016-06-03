package org.neo4j.cypher.internal.spi.next.api

import org.neo4j.cypher.internal.frontend.v3_1.symbols.CypherType

trait RecordLayoutSPI {
  self: KernelTypeSPI with ValueAccessorSPI =>

  type Layout <: AbstractLayout

  // Description of record layout
  trait AbstractLayout {
    self =>

    type Record

    def emptyRecord(): self.Record
    def copy(r: self.Record)
    def column(name: String): ValueAccessor[self.Record]
  }

  // Create a layout for working with records
  def layout(columns: Map[String, CypherType]): self.Layout
}
