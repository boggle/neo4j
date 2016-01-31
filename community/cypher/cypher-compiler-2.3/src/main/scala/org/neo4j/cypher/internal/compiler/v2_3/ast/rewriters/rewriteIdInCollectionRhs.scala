package org.neo4j.cypher.internal.compiler.v2_3.ast.rewriters

import org.neo4j.cypher.internal.frontend.v2_3.ast._
import org.neo4j.cypher.internal.frontend.v2_3.{Rewriter, bottomUp}

/*
This class rewrites id(n) IN ... predicates such that the concrete id values
are properly handled if they evaluate to an Identity
 */
case object rewriteIdInCollectionRhs extends Rewriter {

  override def apply(that: AnyRef) = bottomUp(instance)(that)

  private val instance: Rewriter = Rewriter.lift {
    case expr@In(lhs@FunctionInvocation(_, _, _), rhs)
      if lhs.function.contains(functions.Id) =>

      val newRhs = rhs match {
        case rhs@CollectionSlice(coll@Collection(exprs), _, _) =>
          val newColl = Collection(exprs.map(idExpr => IdentityId(idExpr)(idExpr.position)))(coll.position)
          rhs.copy(collection = newColl)(rhs.position)

        case Collection(exprs) =>
          Collection(exprs.map(idExpr => IdentityId(idExpr)(idExpr.position)))(rhs.position)

        case _ =>
          IdentityIds(rhs)(rhs.position)
      }

      val result = In(lhs, newRhs)(expr.position)
      result
  }
}
