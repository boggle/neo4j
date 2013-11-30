package org.neo4j.cypher.internal.compiler.v2_0.ast

import org.neo4j.cypher.internal.compiler.v2_0.{SemanticState, SemanticCheck, commands, InputToken}
import org.neo4j.cypher.internal.compiler.v2_0.symbols.{AnyType, CollectionType}

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

sealed abstract class Unwind extends AstNode {
  def semanticCheck(expression: Expression): SemanticCheck =
    expression.constrainType(CollectionType(AnyType())) then
    expression.unwindType()

  def toCommand(name: String): commands.Unwind
}

case class RegularUnwind(token: InputToken) extends Unwind {
  override def toString = "UNWIND"

  override def toCommand(name: String): commands.Unwind = commands.RegularUnwind(name)
}

case class OptionalUnwind(token: InputToken) extends Unwind {
  override def toString ="OPTIONAL UNWIND"

  override def toCommand(name: String): commands.Unwind = commands.OptionalUnwind(name)
}
