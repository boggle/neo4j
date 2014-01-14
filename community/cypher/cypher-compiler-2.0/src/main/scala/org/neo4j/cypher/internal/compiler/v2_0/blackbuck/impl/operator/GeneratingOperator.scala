package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.{Scheduler, Operator}
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.{RowPool, Cursor}

abstract class GeneratingOperator[P <: Pos[P], C <: Cursor[P, C], E](key: AnyRef, dest: Operator[P, C, E])
  extends ChainingOperator[P, C, E](key, dest)
{
  override def newActivation(pool: RowPool[P, C], scheduler: Scheduler[P, C, E]): GeneratingActivation[P, C, E]
}
