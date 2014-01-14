package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.{RowPool, Cursor}
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.{Scheduler, Router, Operator}

abstract class ChainingOperator[P <: Pos[P], C <: Cursor[P, C], E](key: AnyRef, val dest: Operator[P, C, E])
  extends AbstractOperator[P, C, E](key)
{
  override def newActivation(pool: RowPool[P, C], scheduler: Scheduler[P, C, E]): ChainingActivation[P, C, E]
}
