/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher

import org.scalatest.Matchers
import org.junit.Test

class UnwindingAcceptanceTest extends ExecutionEngineHelper with Matchers {

  @Test def should_unwind_non_empty_collections() {
    // given
    val result = execute("RETURN UNWIND [1, 2, 3] AS x").columnAs[List[Number]]("x").toSet

    // then
    result should be(Set(1, 2, 3))
  }

  @Test def should_optional_unwind_non_empty_collections() {
    // given
    val result = execute("RETURN OPTIONAL UNWIND [1, 2, 3] AS x").columnAs[List[Number]]("x").toSet

    // then
    result should be(Set(1, 2, 3))
  }

  @Test def should_unwind_empty_collections() {
    // given
    val result = execute("RETURN UNWIND [] AS x").columnAs[List[Number]]("x").toSet

    // then
    result should be(Set())
  }

  @Test def should_optional_unwind_empty_collections() {
    // given
    val result = execute("RETURN OPTIONAL UNWIND [] AS x").columnAs[List[Number]]("x").toSet

    // then
    result should be(Set(null))
  }

  @Test def should_unwind_non_empty_unaliased_collections() {
    // given
    val result = execute("RETURN UNWIND [1, 2]").toSet

    // then
    result should be(Set(Map("[1, 2]" -> 1), Map("[1, 2]" -> 2)))
  }

  @Test def should_unwind_multiple_collections() {
    // given
    val result = execute("RETURN UNWIND [1, 3] AS x, UNWIND [2, 4] AS y").toSet
      .toSet

    // then
    result should be(Set(
      Map("x" -> 1, "y" -> 2),
      Map("x" -> 1, "y" -> 4),
      Map("x" -> 3, "y" -> 2),
      Map("x" -> 3, "y" -> 4)
    ))
  }

  @Test def should_unwind_multiple_collections_2() {
    // given
    val result = execute("WITH UNWIND [1, 3] AS x, UNWIND [2, 4] AS y RETURN x*y AS z").columnAs[List[Number]]("z").toSet

    // then
    result should be(Set(2, 4, 6, 12))
  }

  @Test def should_unwind_multiple_collections_where_one_collection_is_empty() {
    // given
    val result = execute("WITH UNWIND [] AS x, UNWIND [1, 2] AS y RETURN x, y").toSet

    // then
    result should be(Set())
  }

  @Test def should_optional_unwind_multiple_collections_where_one_collection_is_empty() {
    // given
    val result = execute("WITH OPTIONAL UNWIND [] AS x, UNWIND [1, 2] AS y RETURN x, y").toSet

    // then
    result should be(Set(Map("x" -> null, "y" -> 1), Map("x" -> null, "y" -> 2)))
  }

  @Test def should_unwind_together_with_regular_values() {
    // given
    val result = execute("RETURN UNWIND [1, 3] AS x, 12 AS z").toSet

    // then
    result should be(Set(Map("x" -> 1, "z" -> 12), Map("x" -> 3, "z" -> 12)))
  }

  @Test def should_optional_unwind_together_with_regular_values() {
    // given
    val result = execute("RETURN OPTIONAL UNWIND [1, 3] AS x, 12 AS z").toSet

    // then
    result should be(Set(Map("x" -> 1, "z" -> 12), Map("x" -> 3, "z" -> 12)))
  }

  @Test def should_unwind_and_sort() {
    // given
    val result = execute("RETURN UNWIND [3, 2, 1] AS x, 12 aS z ORDER BY x").toList

    // then
    result should be(List(Map("x" -> 1, "z" -> 12), Map("x" -> 2, "z" -> 12), Map("x" -> 3, "z" -> 12)))
  }

  @Test def should_unwind_multiple() {
    // given
    val result = execute("RETURN UNWIND [3, 3, 3] AS x").columnAs[List[Number]]("x").toSet

    // then
    result should be(Set(3, 3, 3))
  }

  @Test def should_unwind_distinct() {
    // given
    val result = execute("RETURN DISTINCT UNWIND [3, 3, 3] AS x, 12 as z").toSet

    // then
    result should be(Set(Map("x" -> 3, "z" -> 12)))
  }

  @Test def should_unwind_and_limit() {
    // given
    val result = execute("RETURN UNWIND [3, 2, 1] AS x, 12 as z LIMIT 1").toList

    // then
    result should be(List(Map("x" -> 3, "z" -> 12)))
  }

  @Test def should_unwind_and_sort_and_limit() {
    // given
    val result = execute("RETURN UNWIND [3, 2, 1] AS x, 12 as z ORDER BY x LIMIT 1").toList

    // then
    result should be(List(Map("x" -> 1, "z" -> 12)))
  }

  @Test def should_reject_unwinding_of_aggregate_expressions() {
    // given
    val n = createNode()
    relate(n, createNode())
    relate(n, createNode())

    // when
    evaluating {
      execute("MATCH (n)-[r]->(m) RETURN n, UNWIND collect(m) AS x").toList
    } should produce[InvalidExpressionException]
  }

  @Test def should_reject_unwinding_of_aggregate_expressions_2() {
    // given
    val n = createNode()
    relate(n, createNode())
    relate(n, createNode())

    // when
    evaluating {
      execute("MATCH (n)-[r]->(m) RETURN n, UNWIND [count(m)] AS x").toList
    } should produce[InvalidExpressionException]
  }

  @Test def should_reject_mixing_aggregate_expressions_and_unwinding() {
=    // when
    evaluating {
      execute("MATCH (n)-[r]->(m) RETURN collect(n), UNWIND [1,2,3] AS x").toList
    } should produce[InvalidExpressionException]
  }
}
