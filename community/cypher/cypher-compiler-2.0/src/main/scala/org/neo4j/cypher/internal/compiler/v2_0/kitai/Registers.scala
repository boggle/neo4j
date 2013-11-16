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

abstract class Register[T] {
  def name: String
}

trait RRegister[+T] extends (Row => T) {
  self: Register[T] =>

  def apply(i: Row): T
}

trait WRegister[-T] {
  self: Register[T] =>

  def update(i: Row, value: T): Unit
}

abstract class RWRegister[T] extends Register[T] with RRegister[T] with WRegister[T]

abstract class Registers(all: Set[Register]) {

  val forAccessing: Set[Register[_]] = all.flatMap {
    case r: Register[_] with RRegister[_] with WRegister[_] => Some(r)
    case _                                                  => None
  }

  val forReading = all.flatMap {
    case r: Register[_] with RRegister[_] => Some(r)
    case _                                => None
  }

  val forWriting = all.flatMap {
    case w: Register[_] with WRegister[_] => Some(w)
    case _                                => None
  }
}