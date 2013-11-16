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
package org.neo4j.cypher.internal.compiler.v2_0.kitai

import org.neo4j.cypher.internal.compiler.v2_0.pipes.QueryState

abstract class Step {
  def apply(row: Row, pool: RowPool)(implicit qs: QueryState) = apply(row, row, pool)

  def apply(first: Row, last: Row, pool: RowPool)(implicit qs: QueryState) = {
    var index = first
    while ( index <= last ) {
      apply(index, pool)
      index = index.nextRow
    }
  }

  def registers: Registers
}

