package org.neo4j.cypher.internal.records

class RecordAllocator {
  private var counter = 0

  def +=(amount: Int) = {
    if (amount <= 0)
      throw new IllegalArgumentException("Positive amount of requested records required")
    val result = counter
    counter += amount
    result
  }

  def size = counter
}