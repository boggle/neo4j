package org.neo4j.cypher.internal.spi.next.impl

import org.neo4j.cypher.internal.spi.next.api.{DatabaseEngineSPI, ValueAccessorSPI}

final case class NeoNode(id: Long) extends AnyVal

class NeoDatabaseEngineSPI(nodes: Map[Long, Map[String,Long]])
  extends DatabaseEngineSPI
    with NeoKernelTypeSPI
    with ValueAccessorSPI
    with DynamicRecordLayoutSPI
    with DynamicValueAccessor {

  // Core SPI for talking to the kernel
  override def nodesGetAll(): NodeIterator = {
    val underlying = nodes.keys.iterator
    new NodeIterator {
      override def hasNext: Boolean = underlying.hasNext
      override def nextNode: Long = underlying.next()
    }
  }
  override def propertyKey(propertyName: String): String = propertyName
  override def nodeGetProperty[L](node: Long, key: String, accessor: ValueAccessor[L], target: L): Unit = {
    accessor.putLongAsInteger(nodes(node).getOrElse(key, -1L), target)
  }


  // Parameter registers (using location ())
  override def parameter(name: String): ValueAccessor[Unit] = ???

  // Additional "dummy" registers (using location ())
  override def value(): ValueAccessor[Unit] = ???

  override def anyToNode(v: Any): Long = v.asInstanceOf[NeoNode].id
  override def nodeToAny(v: Long): Any = NeoNode(v)
}
