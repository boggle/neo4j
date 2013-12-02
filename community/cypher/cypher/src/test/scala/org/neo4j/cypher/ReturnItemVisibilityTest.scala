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

class ReturnItemVisibilityTest extends ExecutionEngineHelper with Matchers {

  @Test def should_see_unaliased_identifier() {
    // given
    val result = execute("WITH 1 AS x, 2 AS y RETURN x, x+y AS y").toSet

    // then
    result should be(Set(Map("x" -> 1, "y" -> 3)))
  }

  @Test def should_hide_unwound_and_unaliased_identifier() {
    // given
    evaluating {
      execute("WITH [1] AS x, [2] AS y RETURN UNWIND y, y as x").toSet
    } should produce[SyntaxException]
  }

  @Test def should_see_identifier_with_same_alias() {
    // given
    val result = execute("WITH 1 AS x, 2 AS y RETURN x AS x, x+y AS y").toSet

    // then
    result should be(Set(Map("x" -> 1, "y" -> 3)))
  }

  @Test def should_see_identifier_with_different_alias() {
    // given
    val result = execute("WITH 1 AS x, 2 AS y RETURN x AS z, x+y AS y").toSet

    // then
    result should be(Set(Map("z" -> 1, "y" -> 3)))
  }

  @Test def should_hide_identifier_alias() {
    evaluating {
      execute("WITH 1 AS x, 2 AS y RETURN x AS z, z+y AS y").toSet
    } should produce[SyntaxException]
  }

  @Test def should_hide_shadowing_expression_alias() {
    evaluating {
      execute("WITH [1] AS x, [2] AS y RETURN x+y AS y, y AS x").toSet
    } should produce[SyntaxException]
  }
}
