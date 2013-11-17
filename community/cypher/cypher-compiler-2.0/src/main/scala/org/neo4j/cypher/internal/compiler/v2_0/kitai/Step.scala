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

abstract class Step {
  def apply(cursor: Cursor, rowPool: RowPool): Cursor
  def registers: Registers
}

case class MapStep[T](register: Register[T], f: T => T) extends Step {

  def apply(cursor: Cursor, rowPool: RowPool): Cursor = {
    val registerA = register(cursor)
    
    for ( row <- cursor ) {
      registerA.value = f(registerA.value)
    }
    cursor.rewind()
  }

  override val registers = Registers(register)
}

case class FilterStep[T](register: Register[T], f: T => Boolean) extends Step {

  def apply(cursor: Cursor, rowPool: RowPool): Cursor =  {
    val registerA = register(cursor)

    for ( row <- cursor ) {
      if (! f(registerA.value)) {
        cursor.remove()
      }
    }
    cursor.rewind()
  }

  override val registers = Registers(Set(register))
}

case class FlatMapStep[T](register: Register[T], f: T => Seq[T]) extends Step {

  def apply(cursor: Cursor, rowPool: RowPool): Cursor =  {
    val registers = cursor.registers
    val inputA = register(cursor)
    val outputCursor = rowPool.newCursor(registers)
    val outputA = register(outputCursor)
    val rowCopier = RowCopier(cursor, outputCursor, registers)

    for ( row <- cursor ) {
      for ( value <- f(inputA.value) ) {
        outputCursor.next()
        rowCopier.copy()
        outputA.value = value
      }
    }
    outputCursor.rewind()
  }

  override val registers = Registers(register)
}

case class FoldStep[A, B](signal: Register[Boolean],
                          input: Register[A], 
                          output: Register[B],
                          initialValue: B,
                          f: (B, A) => B) extends Step {

  var currentValue = initialValue
  
  override def apply(cursor: Cursor, rowPool: RowPool): Cursor = {
    val inputA = input(cursor)
    val signalA = signal(cursor)

    val outputCursor = rowPool.newCursor(Registers(Set(output)))
    val outputA = output(cursor)

    for ( row <- cursor ) {
      currentValue = f(currentValue, inputA.value)
      if (signalA.value) {
        outputCursor.next()
        outputA.value = currentValue
      } 
    }
    
    outputCursor.rewind()
  }

  override val registers = Registers(input, output)
}
