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

import scala.collection.mutable

import scala.reflect.runtime.universe._

trait RowPool {
  def newRowSchemaBuilder: RowSchemaBuilder

  def newRowSchema(registers: Registers): RowSchema = {
    val builder = newRowSchemaBuilder
    for ( register <- registers.all ) {
      builder += register
    }
    builder.result()
  }
}

trait RowSchemaBuilder extends mutable.Builder[Register[_], RowSchema]

//class RowChunkPool(chunkSize: Int) extends RowPool {
//
//  def newRowSchemaBuilder: mutable.Builder[Register[_], RowSchema] = new mutable.Builder[Register[_], RowSchema] {
//    var registerIds: mutable.Map[Register[_], Int] = new mutable.HashMap[Register[_], Int]()
//    var typeCounts: mutable.Map[TypeTag[_], Int] = new mutable.HashMap[TypeTag[_], Int]()
//
//    def +=(elem: Register[_]): this.type = {
//      val typeTag = Specialization(elem.typeTag)
//      val count = typeCounts.get(typeTag).getOrElse(-1) + 1
//      registerIds(elem) = count
//      typeCounts(typeTag) = count
//      this
//    }
//
//    def clear(): Unit = {
//      registerIds.clear()
//      typeCounts.clear()
//    }
//
//    def result(): RowSchema = new RowSchema {
//
//      def registers: Registers = ???
//
//      def newOutput(sizeHint: Int): Cursor = ???
//    }
//  }
//
//}