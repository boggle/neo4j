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
package org.neo4j.cypher.internal.compiler.v2_0.ast

import org.scalatest.Matchers
import org.junit.Test
import org.neo4j.cypher.internal.compiler.v2_0._
import org.neo4j.cypher.internal.compiler.v2_0.symbols.{CollectionType, NumberType}
import org.scalatest.junit.JUnitSuite
import org.neo4j.cypher.internal.compiler.v2_0.DummyToken
import scala.Some

class ReturnItemTest extends JUnitSuite with Matchers {

  val token = DummyToken(0, 1)
  val identifier = Identifier("x", token)

  @Test def should_accept_collection_expressions_in_regular_unwind() {
    // given
    val item = UnaliasedReturnItem(Some(RegularUnwind(token)), identifier, token)
    val state = SemanticState.clean.declareIdentifier(identifier, CollectionType(NumberType())).right.get

    // when
    val errors = item.semanticCheck(state).errors

    // then
    errors should have size 0
  }

  @Test def should_reject_number_expressions_in_regular_unwind() {
    // given
    val item = UnaliasedReturnItem(Some(RegularUnwind(token)), identifier, token)
    val state = SemanticState.clean.declareIdentifier(identifier, NumberType()).right.get

    // when
    val errors = item.semanticCheck(state).errors

    // then
    errors should have size 1
    errors.head.msg should include("Type mismatch: x already defined with conflicting type Number")
  }

  @Test def should_accept_collection_expressions_in_optional_unwind() {
    // given
    val item = UnaliasedReturnItem(Some(OptionalUnwind(token)), identifier, token)
    val state = SemanticState.clean.declareIdentifier(identifier, CollectionType(NumberType())).right.get

    // when
    val errors = item.semanticCheck(state).errors

    // then
    errors should have size 0
  }

  @Test def should_reject_number_expressions_in_optional_unwind() {
    // given
    val item = UnaliasedReturnItem(Some(OptionalUnwind(token)), identifier, token)
    val state = SemanticState.clean.declareIdentifier(identifier, NumberType()).right.get

    // when
    val errors = item.semanticCheck(state).errors

    // then
    errors should have size 1
    errors.head.msg should include("Type mismatch: x already defined with conflicting type Number")
  }
}
