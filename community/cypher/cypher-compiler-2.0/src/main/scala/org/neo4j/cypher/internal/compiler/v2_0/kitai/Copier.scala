package org.neo4j.cypher.internal.compiler.v2_0.kitai

trait Copier {
  def copy()
}

final case class RegisterCopier[@specialized(Specialization.cypherTypes) T](input: Cursor,
                                                                            output: Cursor,
                                                                            register: Register[T]) extends Copier {
  val inputA = register(input)
  val outputA = register(output)

  def copy() {
    outputA.value = inputA.value
  }
}

final case class RowCopier[@specialized(Specialization.cypherTypes) T](input: Cursor,
                                                                       output: Cursor,
                                                                       registers: Registers) extends Copier {
  val copiers = registers.all.map(RegisterCopier(input, output, _))

  def copy() {
    copiers.foreach(_.copy())
  }
}