package org.neo4j.cypher.internal.compiler.v2_0.kitai

trait Cursor extends Iterator[Row] {
  def apply[T](register: Register[T]): Accessor[T]

  def current: Row

  def rewind(): Cursor

  def remove() = ???

  def select(row: Row)

  def registers: Registers
}
