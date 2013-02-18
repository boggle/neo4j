package org.neo4j.cypher.internal.records

abstract class AbstractRecord {

  def factory: RecordFactory[this.type]

  def ev[L <: this.type]: L =:= this.type


  def get(name: String): Any = {
    val f = factory.field[this.type](name)(ev)
//    f.get(this)
    ???
  }

  def set(name: String, newValue: Any): Any = {
//    val f = factory.specialized[this.type].field(name)
//    f.set[this.type](this, newValue)
    ???
  }

//  def clear() {
//    for (field <- factory.fields)
//      field.clear(this)
//  }

//  def assign(record: this.type): this.type = {
//    for (field <- factory.fields)
//      field.set(this, field.get(record))
//    this
//  }

//  def copy = factory.acquire[this.type].assign(this)
}