package org.neo4j.cypher.internal.compiler.v2_0.ast

import org.neo4j.cypher.internal.compiler.v2_0.InputToken
import org.neo4j.cypher.internal.compiler.v2_0.commands

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
final case class Unwind(mode: UnwindMode, token: InputToken) extends AstNode {
  def toCommand(item: commands.ReturnItem): commands.Unwind =
    commands.Unwind(mode, item, item.expression.containsAggregate)
}

object Unwind {
  def regular(token: InputToken) = Unwind(RegularUnwindMode, token)
  def optional(token: InputToken) = Unwind(OptionalUnwindMode, token)
}

