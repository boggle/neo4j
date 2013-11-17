package org.neo4j.cypher.internal.compiler.v2_0.kitai

trait Accessor[@specialized(Specialization.cypherTypes) T] {
  def isNull: Boolean
  def setNull(): Unit

  def value: T
  def value_=(newValue: T)
}