package org.neo4j.cypher.internal.spi.next.api

// Types used for talking to the kernel
trait KernelTypeSPI {
  self =>

  type Entity
  type Node <: Entity
  type Relationship <: Entity
  type PropertyKey

  // Separate interface to ensure it uses an un-boxed Node
  abstract class NodeIterator {
    def foreach(f: self.Node => Unit) {
      while (hasNext) f(nextNode)
    }

    def hasNext: Boolean
    def nextNode: self.Node
  }
}
