package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.operator

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.{RowPool, Cursor}
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched.{Scheduler, Operator}

abstract class UnionOperator[P <: Pos[P], C <: Cursor[P, C], E](key: AnyRef, dest: Operator[P, C, E],
                                                                lhs: Operator[P, C, E],
                                                                rhs: Operator[P, C, E])
  extends FusingOperator[P, C, E](key, dest, lhs, rhs)
{
  override def newActivation(pool: RowPool[P, C], scheduler: Scheduler[P, C, E]): UnionActivation[P, C, E]
}
