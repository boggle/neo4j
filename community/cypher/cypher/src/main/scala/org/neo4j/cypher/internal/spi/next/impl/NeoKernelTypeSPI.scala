package org.neo4j.cypher.internal.spi.next.impl

import org.neo4j.cypher.internal.spi.next.api.KernelTypeSPI

trait NeoKernelTypeSPI extends KernelTypeSPI {
  type Entity = Long
  type Node = Long
  type Relationship = Long

  // For testing purposes
  type PropertyKey = String
}
