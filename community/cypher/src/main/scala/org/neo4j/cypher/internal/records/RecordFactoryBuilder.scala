package org.neo4j.cypher.internal.records

class RecordFactoryBuilder {
  private val allocator = new RecordAllocator()
  private val fieldPosMap = new scala.collection.mutable.HashMap[String, Int]()

  def +=(pair: (String, Int)) {
    val (name, amount) = pair
    if (fieldPosMap `contains` name)
      throw new IllegalArgumentException("Field name already used")

    fieldPosMap(name) = (allocator += amount)
  }

  def +=(name: String) {
    += (name -> 1)
  }

  def build(numRecords: Int): RecordFactory[AbstractRecord] = new DefaultRecordFactory(numRecords)

  final class DefaultRecordFactory(override val size: Int) extends RecordFactory[AbstractRecord] {
    override type RecordType = Record

    val fieldMap  = ( for (pair <- fieldPosMap.toIterable) yield Field.fromPair(pair).toPair ).toMap
    val allFields = fieldMap.values.toIndexedSeq

    def fields[L <: AbstractRecord](implicit ev: L =:= RecordType) = allFields.map(_.lift)
    def field[L <: AbstractRecord](name: String)(implicit ev: L =:= RecordType) = fieldMap(name).lift(ev)

    def acquire[L <: AbstractRecord](implicit ev: RecordType =:= L): L = ev(new Record)

    object Field {
      def fromPair(pair: (String, Int)): Field = new Field(pair._1, pair._2)
    }

    final class Field(override val name: String, private val pos: Int) extends AbstractField[RecordType] {

      def get(record: RecordType): Any = record.data(pos)

      def set(record: RecordType, newValue: Any): Any = {
        val oldValue = record.data(pos)
        record.data(pos) = newValue
        oldValue
      }

      def toPair = name -> this
    }

    final class Record extends AbstractRecord {
      private[DefaultRecordFactory] val data = Array.ofDim[Any](fieldMap.size)

      override val factory = DefaultRecordFactory.this
    }
  }
}