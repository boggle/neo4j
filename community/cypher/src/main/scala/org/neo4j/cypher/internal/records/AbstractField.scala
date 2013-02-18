package org.neo4j.cypher.internal.records

abstract class AbstractField[R] {
  def name: String
  def get(record: R): Any
  def set(record: R, newValue: Any): Any
  def clear(record: R) = set(record, null)

  def lift[L](implicit ev: L =:= R): AbstractField[L] = this.asInstanceOf[AbstractField[L]]
}