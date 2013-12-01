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
package org.neo4j.cypher.internal.compiler.v2_0.pipes

import org.neo4j.cypher.internal.compiler.v2_0.commands.{OptionalUnwind, Unwind}
import org.neo4j.cypher.internal.compiler.v2_0.{PlanDescription, ExecutionContext}
import org.neo4j.cypher.internal.compiler.v2_0.symbols.{CollectionType, AnyType, SymbolTable}
import org.neo4j.cypher.internal.compiler.v2_0.commands.expressions.Identifier
import org.neo4j.cypher.CypherTypeException
import org.neo4j.cypher.internal.compiler.v2_0.data.SimpleVal
import org.neo4j.cypher.internal.helpers.IsCollection

class UnwindPipe(source: Pipe, unwinds: Seq[Unwind]) extends PipeWithSource(source) {

  def throwIfSymbolsMissing(symbols: SymbolTable): Unit =
    unwinds.foreach { (unwind: Unwind) =>
      Identifier(unwind.name).throwIfSymbolsMissing(symbols)
    }

  def symbols: SymbolTable =
    unwinds.foldLeft(source.symbols) { (symbols: SymbolTable, unwind: Unwind) =>
      symbols.evaluateType(unwind.name, CollectionType(AnyType())) match {
        case typ: CollectionType =>
          symbols.replace(unwind.name, typ.iteratedType)
        case typ =>
          throw new CypherTypeException(s"Expected collection for unwinding of ${unwind.name}, but got: $typ")
      }
    }

  def executionPlanDescription: PlanDescription =
    source.executionPlanDescription.andThen(this, "Unwind", "unwinds" -> SimpleVal.fromIterable(unwinds))

  protected def internalCreateResults(input: Iterator[ExecutionContext], state: QueryState): Iterator[ExecutionContext] = {
    unwinds.foldLeft(input) { (iterator: Iterator[ExecutionContext], item: Unwind) =>
      iterator.flatMap { (ctx: ExecutionContext) =>
        val name = item.name
        ctx(name) match {
          case IsCollection(coll) => item match {
            case _: OptionalUnwind if coll.isEmpty => Some(ctx += name -> null)
            case _                                 => coll.map { (elem: Any) => ctx.newWith(name -> elem) }
          }
          case value =>
            throw new CypherTypeException(s"Expected collection for unwinding as $name, but got: $value")
        }
      }
    }
  }
}
