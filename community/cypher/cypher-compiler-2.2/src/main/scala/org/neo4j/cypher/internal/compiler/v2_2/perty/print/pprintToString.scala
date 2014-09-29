/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.cypher.internal.compiler.v2_2.perty.print

import org.neo4j.cypher.internal.compiler.v2_2.perty._
import org.neo4j.cypher.internal.compiler.v2_2.perty.handler.DefaultDocHandler
import org.neo4j.cypher.internal.compiler.v2_2.perty.ops.{AddContent, evalDocOps, expandDocOps}

import scala.reflect.runtime.universe._

object pprintToString {
  // Convert value to String after converting to a doc using the given generator and formatter
  def apply[T : TypeTag](value: T,
                         formatter: DocFormatter = DocFormatters.defaultPageFormatter)
                        (docGen: DocGen[T] = DefaultDocHandler.docGen): String = {
    val docOps = expandDocOps(docGen).apply(Seq(AddContent(value)))
    val doc = evalDocOps(docOps)
    val formatted = formatter(doc)
    val condensed = condense(formatted)
    val result = printCommandsToString(condensed)
    result
  }
}
