package org.neo4j.cypher.internal.compiler.v2_0.prettifier

import org.junit.Assert._
import org.junit.Test

class PrettifierBreakingTest
{
  @Test
  def shouldNotOverspillLongLines() {
    assertIsPrettified("MATCH%n  (veryLongIdentifier)%nREMOVE", "MATCH (veryLongIdentifier) REMOVE", 10)
  }

  private def assertIsPrettified(expected: String, query: String, lineWidth: Int) {
    assertEquals(String.format(expected), Prettifier(query, lineWidth, 2))
  }

}