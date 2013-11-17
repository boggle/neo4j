package org.neo4j.cypher.internal.compiler.v2_0.kitai

import scala.reflect.runtime.universe._

abstract class Accessor[@specialized(Specialization.cypherTypes) T](implicit typeTag: TypeTag[T]) {
  def isNull: Boolean
  def setNull(): Unit

  def value: T
  def value_=(newValue: T)
}