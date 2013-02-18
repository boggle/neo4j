package org.neo4j.cypher.internal.records

import org.scalatest.Assertions
import org.junit.Test

class RecordAllocatorTest extends Assertions {

  @Test
  def incrementsAmount() {
    // GIVEN
    val allocator = new RecordAllocator

    // WHEN
    val first  = allocator += 1
    val second = allocator += 3
    val third = allocator += 1

    // THEN
    assert(0 == first)
    assert(1 == second)
    assert(4 == third)
    assert(5 == allocator.size)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def doesNotDecreaseAmount() {
    // GIVEN
    val allocator = new RecordAllocator

    // THEN
    allocator += -1
  }

  @Test(expected = classOf[IllegalArgumentException])
  def doesNeverDecreaseAmount() {
    // GIVEN
    val allocator = new RecordAllocator

    // THEN
    allocator += 0
  }
}