package org.neo4j.cypher.internal.records

abstract class RecordFactory {
  type Record
  type Field

  class Record {

  }

//  def fields[L <: R](implicit ev: L =:= RecordType): Seq[AbstractField[L]]
//  def field[L <: R](name: String)(implicit ev: L =:= RecordType): AbstractField[L]
//
//  def acquire[L <: R](implicit ev: RecordType =:= L): L
//
//  def size: Int
}