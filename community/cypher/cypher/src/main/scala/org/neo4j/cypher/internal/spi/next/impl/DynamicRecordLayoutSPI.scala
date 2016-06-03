package org.neo4j.cypher.internal.spi.next.impl

import org.neo4j.cypher.internal.frontend.v3_1.symbols.CypherType
import org.neo4j.cypher.internal.spi.next.api.{KernelTypeSPI, ValueAccessorSPI, RecordLayoutSPI}

import scala.collection.mutable

trait DynamicRecordLayoutSPI extends RecordLayoutSPI {
  self: KernelTypeSPI with ValueAccessorSPI with DynamicValueAccessor =>

  type Layout = DynamicLayout

  // Create a layout for working with records
  def layout(columns: Map[String, CypherType]) = new DynamicLayout(columns)

  class DynamicLayout(columns: Map[String, CypherType]) extends AbstractLayout {
    type Record = mutable.Map[String, Any]

    def emptyRecord(): Record = new mutable.HashMap[String, Any]()
    def copy(r: Record) = r.clone()
    def column(name: String): self.ValueAccessor[Record] =
      new DynamicValueAccessor(columns(name), name)
  }
}
