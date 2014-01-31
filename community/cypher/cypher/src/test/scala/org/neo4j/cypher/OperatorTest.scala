/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher

import org.junit.{Ignore, After, Before, Test}
import org.neo4j.graphdb._
import org.neo4j.tooling.GlobalGraphOperations
import org.neo4j.cypher.internal.compiler.v2_1.runtime._
import collection.mutable
import org.neo4j.cypher.performance.MicroBench
import scala.Long

class OperatorTest extends ExecutionEngineHelper {

  var tx: Transaction = _
  var ctx: StatementContext = _
  val registerFactory: RegisterFactory = DirectRegisters.FACTORY

  @Before def init() {
    tx = graph.beginTx()
    ctx = new StatementContext( statement, graph )
  }

  @After def after() {
    tx.close()
  }

  trait TableKey

  case class Long(idx: Int) extends TableKey

  case class Object(idx: Int) extends TableKey

  implicit class RichOperator(inner: Operator) {
    def toList(data: Registers)(implicit table: Map[String, TableKey]): List[Map[String, Any]] = {
      val resultBuilder = List.newBuilder[Map[String, Any]]

      while (inner.next()) {
        val result = new mutable.HashMap[String, Any]()
        table.foreach {
          case (name, Long(idx)) => result += name -> data.entityRegister(idx).getEntity
          case (name, Object(idx)) => result += name -> data.valueRegister(idx).getValue
        }
        resultBuilder += result.toMap
      }

      resultBuilder.result()
    }
  }

  @Test def all_nodes_on_empty_database() {
    val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(1))
    val e0 = data.entityRegister(0)
    val allNodesScan = new AllNodesScanOp(ctx, e0)
    allNodesScan.open()
    assert(allNodesScan.next() === false, "Expected not to find any nodes")
  }

  @Test def all_nodes_on_database_with_three_nodes() {
    val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(1))
    val e0 = data.entityRegister(0)
    val allNodesScan = new AllNodesScanOp(ctx, e0)

    createNode()
    createNode()
    createNode()

    implicit val table = Map("a" -> Long(0))

    assert(allNodesScan.toList(data) === List(Map("a" -> 0), Map("a" -> 1), Map("a" -> 2)))
  }
  
  @Test def labelScan() {
    val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(1))
    val e0 = data.entityRegister(0)

    val labelToken = 0

    createNode()
    val a1 = createLabeledNode("A")
    createLabeledNode("B")
    val a2 = createLabeledNode("A")

    val allNodesScan = new LabelScanOp(ctx, labelToken, e0)

    implicit val table = Map("a" -> Long(0))
    assert(allNodesScan.toList(data) === List(Map("a" -> a1.getId), Map("a" -> a2.getId)))
  }

  @Test def expand() {
    val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(2))
    val e0 = data.entityRegister(0)
    val e1 = data.entityRegister(1)

    val source = createLabeledNode("A")
    val destination = createNode()
    val relId = relate(source, destination).getId

    val lhs = {
      val labelToken = 0
      new LabelScanOp(ctx, labelToken, e0)
    }
    val expand = new ExpandToNodeOp(ctx, lhs, e0, Direction.OUTGOING, e1)

    implicit val table: Map[String, TableKey] = Map("a" -> Long(0), "b" -> Long(1))
    assert(expand.toList(data) === List(Map("a" -> source.getId, "b" -> destination.getId)))
  }


  @Test def hash_join() {
    val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(3))
    val e0 = data.entityRegister(0)
    val e1 = data.entityRegister(1)
    val e2 = data.entityRegister(2)

    for (i <- 0.until(10)) {
      val middle = createLabeledNode("A", "B")
      val lhs = createLabeledNode("A")
      val rhs = createLabeledNode("B")

      if (i >= 2) {
        relate(lhs, middle)
      }

      if (i < 8) {
        relate(rhs, middle)
      }
    }

    val labelAToken = 0
    val labelBToken = 1

    val labelScan1 = new LabelScanOp(ctx, labelAToken, e1)
    val lhs = new ExpandToNodeOp(ctx, labelScan1, e1, Direction.OUTGOING, e0)

    val labelScan2 = new LabelScanOp(ctx, labelBToken, e2)
    val rhs = new ExpandToNodeOp(ctx, labelScan2, e2, Direction.OUTGOING, e0)

    val lhsTail = new DirectRegisters( Array.empty, Array(e1) )
    val hashJoin = new HashJoinOnNodeOp(ctx, e0, lhsTail, lhs, rhs)

    implicit val table = Map("a" -> Long(0), "b" -> Long(1), "c" -> Long(2))

    assert(hashJoin.toList(data).size === 6)
  }

  @Test def hash_join2() {
    val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(3))
    val e0 = data.entityRegister(0)
    val e1 = data.entityRegister(1)
    val e2 = data.entityRegister(2)
    val b = createNode()
    
    for(i <- 0 until 4) {
      val a = createLabeledNode("A")
      relate(a, b)
    }
    for(i <- 0 until 4) {
      val a = createLabeledNode("B")
      relate(a, b)
    }

    val labelAToken = 0
    val labelBToken = 1

    val labelScan1 = new LabelScanOp(ctx, labelAToken, e1)
    val lhs = new ExpandToNodeOp(ctx, labelScan1, e1, Direction.OUTGOING, e0)

    val labelScan2 = new LabelScanOp(ctx, labelBToken, e2)
    val rhs = new ExpandToNodeOp(ctx, labelScan2, e2, Direction.OUTGOING, e0)

    val lhsTail = new DirectRegisters( Array.empty, Array(e1) )
    val hashJoin = new HashJoinOnNodeOp(ctx, e0, lhsTail, lhs, rhs)

    implicit val table = Map("a" -> Long(0), "b" -> Long(1), "c" -> Long(2))

    assert(hashJoin.toList(data).size === 16)
  }

  @Ignore
  @Test def performance_of_node_expand() {
    // GIVEN A GRAPH
    for (i <- 0.until(10000)) {
      val source = createLabeledNode("A")
      for (j <- 0.until(10)) {
        val destination = createNode()
        relate(source, destination)
      }
    }
    tx.success()
    tx.close()

    abstract class ExpandMicroBench(name: String) extends MicroBench(name, 10, 5, 90)

    object OldCypherExpandMicroBench extends ExpandMicroBench("Old Cypher") {
      def run(): Unit = {
        init()
        execute("match (a:A)-[r]->(b) return id(b)").toList
        after()
      }
    }

    object CoreAPIExpandMicroBench extends ExpandMicroBench("Core API") {
      val label = DynamicLabel.label("A")

      def run(): Unit = {
        val result = Array.newBuilder[scala.Long]
        init()
        val allNodes: ResourceIterable[Node] = GlobalGraphOperations.at(graph).getAllNodesWithLabel(label)
        val nodes: ResourceIterator[Node] = allNodes.iterator()
        while (nodes.hasNext) {
          val current = nodes.next()
          val relationships = current.getRelationships(Direction.OUTGOING).iterator()
          while(relationships.hasNext) {
            result += relationships.next().getEndNode.getId
          }
        }
        after()
        result.result()
      }
    }

    object NewCypherExpandMicroBench extends ExpandMicroBench("New Cypher") {
      val labelToken = 0
      val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(2))
      val e0 = data.entityRegister(0)
      val e1 = data.entityRegister(1)

      def run(): Unit = {
        val lhs = new LabelScanOp(ctx, labelToken, e0)
        val expand = new ExpandToNodeOp(ctx, lhs, e0, Direction.OUTGOING, e1)
        val result = Array.newBuilder[scala.Long]
        init()
        expand.open()
        while (expand.next()) {
          result += e1.getEntity.toLong
        }
        expand.close()
        after()
        result.result()
      }
    }

    println(OldCypherExpandMicroBench.result)
    println(CoreAPIExpandMicroBench.result)
    println(NewCypherExpandMicroBench.result)
  }

  @Ignore
  @Test def performance_of_rel_expand() {
    // GIVEN A GRAPH
    for (i <- 0.until(10000)) {
      val source = createLabeledNode("A")
      for (j <- 0.until(10)) {
        val destination = createNode()
        relate(source, destination)
      }
    }
    tx.success()
    tx.close()

    abstract class ExpandMicroBench(name: String) extends MicroBench(name, 10, 5, 90)

    object OldCypherExpandMicroBench extends ExpandMicroBench("Old Cypher") {
      def run(): Unit = {
        init()
        execute("match (a:A)-[r]->() return id(r)").toList
        after()
      }
    }

    object CoreAPIExpandMicroBench extends ExpandMicroBench("Core API") {
      val label = DynamicLabel.label("A")

      def run(): Unit = {
        val result = Array.newBuilder[scala.Long]
        init()
        val allNodes: ResourceIterable[Node] = GlobalGraphOperations.at(graph).getAllNodesWithLabel(label)
        val nodes: ResourceIterator[Node] = allNodes.iterator()
        while (nodes.hasNext) {
          val current = nodes.next()
          val relationships = current.getRelationships(Direction.OUTGOING).iterator()
          while(relationships.hasNext) {
            result += relationships.next().getId
          }
        }
        after()
        result.result()
      }
    }

    object NewCypherExpandMicroBench extends ExpandMicroBench("New Cypher") {
      val labelToken = 0
      val data = registerFactory.createRegisters(RegisterSignature.newWithEntityRegisters(2))
      val e0 = data.entityRegister(0)
      val e1 = data.entityRegister(1)

      def run(): Unit = {
        val lhs = new LabelScanOp(ctx, labelToken, e0)
        val expand = new ExpandToRelOp(ctx, lhs, e0, Direction.OUTGOING, e1)
        val result = Array.newBuilder[scala.Long]
        init()
        expand.open()
        while (expand.next()) {
          result += e1.getEntity.toLong
        }
        expand.close()
        after()
        result.result()
      }
    }

    println(OldCypherExpandMicroBench.result)
    println(CoreAPIExpandMicroBench.result)
    println(NewCypherExpandMicroBench.result)
  }

  @Ignore
  @Test def performance_of_register_access() {
    
    abstract class RegisterAccessBench(name: String) extends MicroBench(name, 10000, 0, 9000, unit = 1000L) {
      var count = 0L

      def run(): Unit = {
        (0 until 100000).foreach { _ =>
          //prepare(Math.abs(count.toInt % 10))
          access()
        }
      }

      def prepare(idx: Int)

      def access(): Unit
    }
    
    object DirectAccess extends RegisterAccessBench("Direct") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )
      var register = registers.entityRegister(0)

      def prepare(idx: Int): Unit = {
        register = registers.entityRegister(idx)
      }

      def access(): Unit = {
        register.setEntity(count + 1L)
        count = register.getEntity
      } 
    }
    
    object IndirectAccess extends RegisterAccessBench("Indirect") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )
      var idx = 0

      def prepare(idx: Int): Unit = {
        this.idx = idx
      }

      def access(): Unit = {
        val register = registers.entityRegister(idx)
        register.setEntity(count + 1L)
        count = register.getEntity
      }
    }

    object ArrayAccess extends RegisterAccessBench("Array") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )
      val snapshot = new RegisterSnapshot( registers )
      var idx = 0

      def prepare(idx: Int): Unit = {
        this.idx = idx
      }

      def access(): Unit = {
        snapshot.setEntityValue(idx, count + 1L)
        count = snapshot.getEntityValue(idx)
      }
    }

    object UnsafeArrayAccess extends RegisterAccessBench("Unsafe Array") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )
      val snapshot = new RegisterSnapshot( registers )
      var idx = 0

      def prepare(idx: Int): Unit = {
        this.idx = idx
      }

      def access(): Unit = {
        snapshot.setUnsafeEntityValue(idx, count + 1L)
        count = snapshot.getUnsafeEntityValue(idx)
      }
    }

    println(UnsafeArrayAccess.result)
    println(IndirectAccess.result)
    println(ArrayAccess.result)
    println(DirectAccess.result)

    println("again")

    println(UnsafeArrayAccess.result)
    println(IndirectAccess.result)
    println(ArrayAccess.result)
    println(DirectAccess.result)

    println("again")

    println(UnsafeArrayAccess.result)
    println(DirectAccess.result)
    println(IndirectAccess.result)
    println(ArrayAccess.result)


    println("again")

    println(ArrayAccess.result)
    println(DirectAccess.result)
    println(UnsafeArrayAccess.result)
    println(IndirectAccess.result)
  }

  @Ignore
  @Test def performance_of_seq_register_copy() {
    abstract class RegisterCopyBench(name: String) extends MicroBench(name, 10000, 0, 9000, unit = 1000L) {
      var count = 0L

      def run(): Unit = {
        (0 until 10000).foreach { _ =>
          access()
        }
      }

      def access(): Unit
    }

    object RegisterCopy extends RegisterCopyBench("Direct") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )

      def access(): Unit = {
        registers.copy()
      }
    }

    object ArrayCopy extends RegisterCopyBench("Array") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )
      val snapshot = new RegisterSnapshot( registers )

      def access(): Unit = {
        snapshot.copy()
      }
    }

    object ArrayCopy2 extends RegisterCopyBench("Array2") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )
      val snapshot = new RegisterSnapshot( registers )

      def access(): Unit = {
        snapshot.copy2()
      }
    }
    println(RegisterCopy.result)
    println(ArrayCopy.result)
    println(ArrayCopy2.result)

    println("again")

    println(RegisterCopy.result)
    println(ArrayCopy.result)
    println(ArrayCopy2.result)

    println("again")

    println(RegisterCopy.result)
    println(ArrayCopy.result)
    println(ArrayCopy2.result)
  }

  @Ignore
  @Test def performance_of_rand_register_copy() {
    abstract class RegisterCopyBench(name: String) extends MicroBench(name, 10000, 0, 9000, unit = 1000L) {
      var count = 0L

      val scan = Array(7, 3, 1, 2, 6, 5, 4, 9, 8, 0)

      def run(): Unit = {
        (0 until 10000).foreach { _ =>
          access()
        }
      }

      def access(): Unit
    }

    object RegisterCopy extends RegisterCopyBench("Copy Registers") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) ).asInstanceOf[DirectRegisters]

      def access(): Unit = {
        val copy = new DirectRegisters( registers.signature() )
        copy.setEntitiesFrom( registers, scan )
      }
    }

    object RegisterToArrayCopy extends RegisterCopyBench("Copy Registers to Array") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) ).asInstanceOf[DirectRegisters]

      def access(): Unit = {
        val copy = new RegisterSnapshot( RegisterSnapshot.NO_VALUES, registers.copyEntities(scan) )
      }
    }

    object ArrayCopy extends RegisterCopyBench("Copy Arrays") {
      val registers = DirectRegisters.FACTORY.createRegisters( RegisterSignature.newWithEntityRegisters(10) )
      val snapshot = new RegisterSnapshot( registers )

      def access(): Unit = {
        val copy = new RegisterSnapshot( RegisterSnapshot.NO_VALUES, Array.ofDim(10) )
        var i = 0
        while (i < 10)
        {
          val idx = scan(i)
          copy.setEntityValue( idx, snapshot.getEntityValue( idx ) )
          i += 1
        }
      }
    }

    println(RegisterCopy.result)
    println(RegisterToArrayCopy.result)
    println(ArrayCopy.result)

    println("again")

    println(RegisterCopy.result)
    println(RegisterToArrayCopy.result)
    println(ArrayCopy.result)

    println("again")

    println(RegisterCopy.result)
    println(RegisterToArrayCopy.result)
    println(ArrayCopy.result)
  }
}


