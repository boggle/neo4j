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
package org.neo4j.cypher.internal.compiler.v2_0.parser

import org.neo4j.cypher.internal.compiler.v2_0.{commands, ast}
import org.neo4j.cypher.internal.compiler.v2_0.commands.expressions

import org.junit.Test
import org.parboiled.scala._
import org.neo4j.cypher.internal.compiler.v2_0.ast.{UnwindMode, OptionalUnwindMode, RegularUnwindMode, ReturnItem}
import org.scalatest.junit.JUnitSuite

class ReturnItemParserTest extends JUnitSuite with ParserTest[ast.ReturnItem, (commands.ReturnItem, Option[commands.Unwind])] with Query with Expressions {
  implicit val parserToTest = ReturnItem ~ EOI

  @Test def should_support_regular_unwinding() {
    parsing("UNWIND x") shouldGive unaliasedItem("x", Some(RegularUnwindMode))
    parsing("UNWIND x AS y") shouldGive aliasedItem("x", "y", Some(RegularUnwindMode))
  }

  @Test def should_support_optional_unwinding() {
    parsing("OPTIONAL UNWIND x") shouldGive unaliasedItem("x", Some(OptionalUnwindMode))
    parsing("OPTIONAL UNWIND x AS y") shouldGive aliasedItem("x", "y", Some(OptionalUnwindMode))
  }

  @Test def should_support_no_unwinding() {
    parsing("x") shouldGive unaliasedItem("x", None)
    parsing("x AS y") shouldGive aliasedItem("x", "y", None)
  }

  def convert(astNode: ReturnItem): (commands.ReturnItem, Option[commands.Unwind]) = (astNode.toCommand, astNode.toUnwindCommand)

  def unaliasedItem(identifier: String, optUnwindMode: Option[UnwindMode]) = {
    val item: commands.ReturnItem = commands.ReturnItem(expressions.Identifier(identifier), identifier, renamed = false)
    val optUnwind = optUnwindMode.map(commands.Unwind(_, item, containedAggregate = false))
    (item, optUnwind)
  }

  def aliasedItem(identifier: String, name: String, optUnwindMode: Option[UnwindMode]) = {
    val item: commands.ReturnItem = commands.ReturnItem(expressions.Identifier(identifier), name, renamed = true)
    val optUnwind = optUnwindMode.map(commands.Unwind(_, item, containedAggregate = false))
    (item, optUnwind)
  }
}
