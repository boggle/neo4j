package org.neo4j.cypher.internal.spi.next

import org.neo4j.cypher.internal.spi.next.api.DatabaseEngineSPI
import org.neo4j.cypher.internal.spi.next.impl.NeoDatabaseEngineSPI
import org.neo4j.cypher.internal.frontend.v3_1.symbols._

class Demo(propertyName: String, columnName: String) {
  def apply[S <: DatabaseEngineSPI]()(implicit spi: S): Seq[String] = {
    val propertyKey = spi.propertyKey(propertyName)
    val layout = spi.layout(Map(columnName -> CTAny))
    val accessor = layout.column(columnName)
    val record = layout.emptyRecord()
    val all = spi.nodesGetAll()
    val result = Seq.newBuilder[String]
    all.foreach { node =>
      spi.nodeGetProperty(node, propertyKey, accessor, record)
      result += accessor.getValueAsTextFrom(record)
    }
    result.result()
  }
}


object Demo {
  def main(args: Array[String]) = {
    val spi = new NeoDatabaseEngineSPI(nodes = Map(
      1L -> Map("foo" -> 12L),
      2L -> Map("foo" -> 13L, "bar" -> 14L),
      3L -> Map("bar" -> 15L),
      4L -> Map("baz" -> 16L)
    ))

    val demo = new Demo("foo", "bar")
    val result = demo()(spi)
    if (result == Seq("12", "13", "-1", "-1"))
      println("Success!")
    else
      throw new IllegalStateException(s"Demo failed with: $result")
  }
}
