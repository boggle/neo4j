package org.neo4j.cypher.internal.compiler.v2_0.kitai

trait RowSchema {
  def newOutput(sizeHint: Int): Cursor
  def registers: Registers
}