package org.neo4j.cypher.internal

import org.scalatest.Matchers
import org.neo4j.cypher.ExecutionEngineHelper
import org.junit.Test

class UnwindingAcceptanceTest extends ExecutionEngineHelper with Matchers {

  @Test def should_unwind_non_empty_collections() {
    // given
    val result = execute("RETURN UNWIND [1, 2, 3] AS x").columnAs[List[Number]]("x").toList

    // then
    result should be(List(1, 2, 3))
  }
}
