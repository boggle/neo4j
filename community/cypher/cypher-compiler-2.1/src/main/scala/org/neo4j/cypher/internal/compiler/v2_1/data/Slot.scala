package org.neo4j.cypher.internal.compiler.v2_1.data

trait SlotReader[T] extends (Unit => T) {
  final override def apply(v: Unit): T = apply()

  def apply():T
}

sealed abstract class Stage {
  type Slot[T] <: SlotReader[T]

  def newEmptySlot[T]: Slot[T] = newSlot[T](None)
  def newValueSlot[T](initialValue: T): Slot[T] = newSlot(Option(initialValue))

  def newSlot[T](initialValue: Option[T]): Slot[T]

  def get[T](slot: Slot[T]) = slot()

  def set[T](slot: Slot[T], value: T)
}

sealed abstract class StageImpl extends Stage {
  override type Slot[T] = SlotImpl[T]

  def newSlot[T](initialValue: Option[T]): Slot[T] =  new SlotImpl[T](initialValue)

  def set[T](slot: Slot[T], newValue: T) {
    slot._value = Option(newValue)
  }

  class SlotImpl[T](initialValue: Option[T]) extends SlotReader[T] {
    private[StageImpl] var _value = initialValue
    def apply(): T = initialValue.get
  }
}

object RewritingStage extends StageImpl
object TypingStage extends StageImpl
