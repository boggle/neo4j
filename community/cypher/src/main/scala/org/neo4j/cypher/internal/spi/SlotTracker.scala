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
package org.neo4j.cypher.internal.spi

import scala.collection.mutable
import org.neo4j.cypher.CypherException
import org.neo4j.cypher.internal.ExecutionContext
import org.neo4j.cypher.internal.commands.expressions.{SlotIdentifier, Identifier, Expression}


sealed abstract class Slot extends PartialFunction[ExecutionContext, Any] {
  def get(m: ExecutionContext): Option[Any]
  def set(m: ExecutionContext, value: Any): Unit
  def remove(m: ExecutionContext): Unit
}

abstract class SlotTracker extends (String => Option[Slot]) {
  def +=(name: String): Slot
  def -=(name: String): Unit
  def +?=(name: String): Slot
  def get(name: String): Slot

  // def mapExpression(e: Expression): Expression = e

  def mapExpression(e: Expression): Expression = e match {
    case i @ Identifier(entityName) if i.slot.isEmpty =>
      apply(entityName) match {
        case Some(slot) => SlotIdentifier(entityName, slot)
        case None       => i
      }
    case _ =>
      e
  }

  def names: Iterable[String]
  def slots: Iterable[Slot]

  def reset(): Unit
}

sealed abstract class SlotException(slotName: String, message: String) extends CypherException(message, null)

class DuplicateSlotException(val slotName: String, val slot: Slot)
  extends SlotException(slotName, s"Slot for '$slotName' already exists")

class SlotNotFoundException(val slotName: String)
  extends SlotException(slotName, s"Slot for '$slotName' not found")

final case class NameSlot(name: String) extends Slot {
  def isDefinedAt(m: ExecutionContext): Boolean = m.contains(name)
  def apply(m: ExecutionContext): Any = m(name)
  def get(m: ExecutionContext): Option[Any] = m.get(name)
  def set(m: ExecutionContext, value: Any): Unit = m += name -> value
  def remove(m: ExecutionContext): Unit = m -= name
}

class NameSlotTracker extends SlotTracker {
  private val m: collection.mutable.Map[String, NameSlot] = new mutable.LinkedHashMap[String, NameSlot]

  def +?=(name: String): Slot = m.get(name) match {
    case Some(slot) => slot
    case None       => +=(name)
  }

  def +=(name: String): Slot =
    if (m.contains(name)) {
      throw new DuplicateSlotException(name, m(name))
    } else {
      val slot = NameSlot(name)
      m += name -> slot
      slot
    }

  def -=(name: String): Unit =
    if (m.contains(name)) {
      m -= name
    } else {
      throw new SlotNotFoundException(name)
    }

  def apply(name: String): Option[Slot] = m.get(name)

  def get(name: String): Slot = apply(name) match {
    case Some(slot) => slot
    case None       => throw new SlotNotFoundException(name)
  }

  def reset() {
    m.clear()
  }

  def names: Iterable[String] = m.keys

  def slots: Iterable[Slot] = m.values
}