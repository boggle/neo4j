/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.cypher.internal.compiler.v3_0.planner.logical.plans

import org.neo4j.cypher.internal.compiler.v3_0.planner.{CardinalityEstimation, PlannerQuery}
import org.neo4j.cypher.internal.frontend.v3_0.ast.{Expression, ResolvedCall}

case class CallProcedure(left: LogicalPlan,
                         call: ResolvedCall)
                        (val solved: PlannerQuery with CardinalityEstimation)
  extends LogicalPlan with LazyLogicalPlan {
  val lhs = Some(left)
  def rhs = None

  def availableSymbols: Set[IdName] = left.availableSymbols ++ call.callResults.map { result => IdName.fromVariable(result.variable) }

  override def mapExpressions(f: (Set[IdName], Expression) => Expression): LogicalPlan = {
    val leftSymbols = left.availableSymbols
    val exprMapper = (expr: Expression) => f(leftSymbols, expr)
    val newLeft = left.mapExpressions(f)
    val newCall = call.copy(
      callArguments = call.callArguments.map(exprMapper)
    )(
      call.signature,
      call.declaredArguments.map(_.map(exprMapper)),
      call.declaredResults
    )(call.position)

    copy(left = newLeft, call = newCall)(solved)
  }
}
