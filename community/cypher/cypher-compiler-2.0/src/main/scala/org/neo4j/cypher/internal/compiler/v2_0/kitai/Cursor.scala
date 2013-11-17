package org.neo4j.cypher.internal.compiler.v2_0.kitai

trait Cursor extends Iterator[Row] {
  def apply[@specialized(Specialization.cypherTypes) T](register: Register[T]): Accessor[T] = rowSchema(register)

  def current: Row

  def remove() = ???

  def select(row: Row)

  def chomp(): Cursor

  def rewind(): Cursor

  def rowSchema: RowSchema
}
