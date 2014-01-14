package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.{RowPool, Cursor}
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.{Scheduler, Activation, Operator}

abstract class AbstractOperator[P <: Pos[P], C <: Cursor[P, C], E](val key: AnyRef) extends Operator[P, C, E] {
  override def newActivation(pool: RowPool[P, C], scheduler: Scheduler[P, C, E]): AbstractActivation[P, C, E]
}
