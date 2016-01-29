/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.cypher.internal.compiler.v2_3.commands.expressions

import org.neo4j.cypher.internal.compiler.v2_3._
import org.neo4j.cypher.internal.compiler.v2_3.helpers.IsCollection
import org.neo4j.cypher.internal.compiler.v2_3.pipes.QueryState
import org.neo4j.cypher.internal.compiler.v2_3.symbols.SymbolTable
import org.neo4j.cypher.internal.frontend.v2_3.CypherTypeException
import org.neo4j.cypher.internal.frontend.v2_3.symbols._
import org.neo4j.graphdb.{Node, Relationship}

case class IdFunction(inner: Expression) extends NullInNullOutExpression(inner) {
  def compute(value: Any, m: ExecutionContext)(implicit state: QueryState) =
    state.query.entityId(
      value,
      x => throw new CypherTypeException(
        "Expected `%s` to be a node or relationship, but it was `%s`".format(inner, x.getClass.getSimpleName))
    )

  def rewrite(f: (Expression) => Expression) = f(IdFunction(inner.rewrite(f)))

  def arguments = Seq(inner)

  def calculateType(symbols: SymbolTable): CypherType = CTInteger

  def symbolTableDependencies = inner.symbolTableDependencies
}

case class IdentityId(inner: Expression) extends NullInNullOutExpression(inner) {
  def compute(value: Any, m: ExecutionContext)(implicit state: QueryState) =
    state.query.identityId(value, identity)

  def rewrite(f: (Expression) => Expression) = f(IdentityId(inner.rewrite(f)))

  def arguments = Seq(inner)

  def calculateType(symbols: SymbolTable): CypherType = CTAny

  def symbolTableDependencies = inner.symbolTableDependencies
}

case class IdentityIds(inner: Expression) extends NullInNullOutExpression(inner) {
  def compute(value: Any, m: ExecutionContext)(implicit state: QueryState) = value match {
      case IsCollection(coll) => coll.map(state.query.identityId(_, identity))
      case other => other
  }

  def rewrite(f: (Expression) => Expression) = f(IdentityId(inner.rewrite(f)))

  def arguments = Seq(inner)

  def calculateType(symbols: SymbolTable): CypherType = CTCollection(CTAny)

  def symbolTableDependencies = inner.symbolTableDependencies
}
