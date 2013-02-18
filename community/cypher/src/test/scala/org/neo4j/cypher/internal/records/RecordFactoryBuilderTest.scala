package org.neo4j.cypher.internal.records

import org.scalatest.Assertions
import org.junit.Test

class RecordFactoryBuilderTest extends Assertions {

  @Test
  def buildSimpleFactory() {
    // GIVEN
    val builder = new RecordFactoryBuilder()

    // WHEN
    builder += "foo"
    builder += "bar"
    builder += "baz"
    val factory = builder.build(10)

    // THEN
    assert(10 === factory.size)
    // assert(3 === factory.fieldNames.size)
  }

  @Test
  def CRUDfactoryRecord() {
    // GIVEN
    val builder = new RecordFactoryBuilder()
    builder += "foo"
    builder += "bar"
    builder += "baz"
    val factory = builder.build(10)

    // WHEN
    val record = factory.acquire
    record.field("foo").set(record, 12)

    // THEN
    assert(12 === record.field("foo").get(record))
  }

}