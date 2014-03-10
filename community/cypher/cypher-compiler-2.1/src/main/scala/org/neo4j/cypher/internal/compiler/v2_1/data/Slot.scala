package org.neo4j.cypher.internal.compiler.v2_1.data

trait Slot[T] {
  final def apply() = get

  def get: T
}

sealed class Stage

object Stage {

  class SimpleSlot[T](protected var value: T) extends Slot[T] {
    def get = Option(value).get
    protected[Stage] def set(newValue: T) { value = newValue }
  }

  object Typing extends Stage {
    def newTypeSlot[T](initialValue: T) = new TypeSlot[T](initialValue)
    def setTypeSlot[T](slot: TypeSlot[T], newValue: T) = slot.set(newValue)

    class TypeSlot[T](initialValue: T) extends SimpleSlot[T](initialValue)
  }


  object Rewriting extends Stage {
    def newTypeSlot[T](initialValue: T) = new RewritingSlot[T](initialValue)
    def setTypeSlot[T](slot: RewritingSlot[T], newValue: T) = slot.set(newValue)

    class RewritingSlot[T](initialValue: T) extends SimpleSlot[T](initialValue)
  }
}

object Foo {

  def foo(a: Stage.Typing.TypeSlot[Int]) {
    val b = Stage.Rewriting.newTypeSlot[Int](12)
    Stage.Rewriting.setTypeSlot(b, 12)
    Stage.Typing.setTypeSlot(a, a.get)
  }
}
