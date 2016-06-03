package org.neo4j.cypher.internal.spi.next.api

trait DatabaseEngineSPI extends KernelTypeSPI with ValueAccessorSPI with RecordLayoutSPI {
  self =>

  // Core SPI for talking to the kernel
  def nodesGetAll(): self.NodeIterator
  def nodeGetProperty[L](node: self.Node, key: self.PropertyKey, accessor: ValueAccessor[L], target: L)
  def propertyKey(propertyName: String): self.PropertyKey

  // Parameter registers (using location ())
  def parameter(name: String): self.ValueAccessor[Unit]

  // Additional "dummy" registers (using location ())
  def value(): self.ValueAccessor[Unit]
}




