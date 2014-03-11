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
package org.neo4j.cypher.internal.compiler.v2_1

import java.lang.reflect.Method
import scala.collection.mutable.{HashMap => MutableHashMap}
import org.neo4j.cypher.internal.compiler.v2_1.symbols.TypeSpec
import org.neo4j.cypher.InternalException

case class RewritingContext(state: SemanticState) {
  def apply(typeChanges: Seq[(ast.Expression, TypeSpec)]) = {
    val resultState =typeChanges.foldLeft(state){ (state: SemanticState, update: (ast.Expression, TypeSpec)) =>
      val (expr, typeSpec) = update
      state.specifyType(expr, typeSpec) match {
        case Right(newState) => newState
        case Left(semError)  => throw new InternalException(semError.msg)
      }
    }
    copy(state = resultState)
  }
}

object RewritingContext {
  def empty = RewritingContext(state = SemanticState.clean)
}

object Rewriter {
  implicit class LiftedRewriter(f: (AnyRef => Option[AnyRef])) extends Rewriter {
    def apply(v: (RewritingContext, AnyRef)): (RewritingContext, AnyRef) = v match {
      case (context, astNode) => f(astNode) match {
        case Some(replacement) => (context, replacement)
        case None              => v
      }
    }
  }

  def typedLift(f: PartialFunction[AnyRef, RewriteResult]): Rewriter = new Rewriter {
    def apply(v: (RewritingContext, AnyRef)): (RewritingContext, AnyRef) =
      if (f.isDefinedAt(v))
        f(v) match {
          case DoRewrite(replacement, typeChanges) =>
            (v._1(typeChanges), replacement)
          case DoNotRewrite =>
            v
        }
    else
        v
  }

  def lift(f: PartialFunction[AnyRef, AnyRef]): Rewriter = new Rewriter {
    def apply(v: (RewritingContext, AnyRef)): (RewritingContext, AnyRef) = v match {
      case (context, astNode) if f.isDefinedAt(astNode) => (context, f(astNode))
      case _                                            => v
    }
  }
}

sealed trait RewriteResult
final case class DoRewrite(replacement: AnyRef, typeChanges: Seq[(ast.Expression, TypeSpec)] = Seq.empty) extends RewriteResult
case object DoNotRewrite extends RewriteResult

trait Rewriter extends (((RewritingContext, AnyRef)) => (RewritingContext, AnyRef))


object Rewritable {
  implicit class IteratorEq[A <: AnyRef](val iterator: Iterator[A]) {
    def eqElements[B <: AnyRef](that: Iterator[B]): Boolean = {
      while (iterator.hasNext && that.hasNext) {
        if (!(iterator.next eq that.next))
          return false
      }
      !iterator.hasNext && !that.hasNext
    }
  }

  implicit class DuplicatableAny(val that: AnyRef) extends AnyVal {
    def dup(context: RewritingContext)(rewriter: ((RewritingContext, AnyRef)) => (RewritingContext, AnyRef)): (RewritingContext, AnyRef) = that match {
      case p: Product with AnyRef =>
        val (rewrittenContext, rewrittenChildren) = DuplicatableAny.foldMap(context)(rewriter)(p.productIterator.asInstanceOf[Iterator[AnyRef]].toSeq)

        if (p.productIterator.asInstanceOf[Iterator[AnyRef]] eqElements rewrittenChildren.iterator)
          (rewrittenContext, p)
        else
          p.dup(rewrittenChildren.toSeq).asInstanceOf[(RewritingContext, AnyRef)]
      case s: IndexedSeq[_] =>
        DuplicatableAny.foldMap(context)(rewriter)(s.asInstanceOf[IndexedSeq[AnyRef]])
      case s: Seq[_] =>
        DuplicatableAny.foldMap(context)(rewriter)(s.asInstanceOf[Seq[AnyRef]])
      case t =>
        (context, t)
    }
  }

  object DuplicatableAny {
    def foldMap[A, B](init: A)(f: ((A, B)) => (A, B))(items: Iterable[B]): (A, Iterable[B]) = {
      var current = init
      val mapped = items.map { item =>
        val (updatedCurrent, updatedItem) = f((current, item))
        current = updatedCurrent
        updatedItem
      }
      (current, mapped)
    }
  }

  private val productCopyConstructors = new ThreadLocal[MutableHashMap[Class[_], Method]]() {
    override def initialValue: MutableHashMap[Class[_], Method] = new MutableHashMap[Class[_], Method]
  }

  implicit class DuplicatableProduct(val product: Product) extends AnyVal {
    def dup(children: Seq[AnyRef]): Product = product match {
      case a: Rewritable =>
        a.dup(children)
      case _ =>
        copyConstructor.invoke(product, children.toSeq: _*).asInstanceOf[Product]
    }

    def copyConstructor: Method = {
      val productClass = product.getClass
      productCopyConstructors.get.getOrElseUpdate(productClass, productClass.getMethods.find(_.getName == "copy").get)
    }
  }

  implicit class RewritableAny(val that: AnyRef) extends AnyVal {
    def rewrite(rewriter: Rewriter): AnyRef = {
      val (_, result) = rewrite(RewritingContext.empty)(rewriter)
      result
    }

    def rewrite(rewritingContext: RewritingContext)(rewriter: Rewriter): (RewritingContext, AnyRef) = rewriter.apply((rewritingContext, that))
  }
}

trait Rewritable {
  def dup(children: Seq[AnyRef]): this.type
}

case class topDown(rewriters: Rewriter*) extends Rewriter {
  import Rewritable._

  def apply(that: (RewritingContext, AnyRef)): (RewritingContext, AnyRef) = {
    val (rewrittenContext, rewrittenThat) = rewriters.foldLeft(that) {
      case ((context, t), r) => t.rewrite(context)(r)
    }

    rewrittenThat.dup(rewrittenContext)( pair => this.apply(pair))
  }
}

case class bottomUp(rewriters: Rewriter*) extends Rewriter {
  import Rewritable._

  def apply(v: (RewritingContext, AnyRef)): (RewritingContext, AnyRef) = {
    val (initialContext, that) = v
    val rewrittenThat = that.dup(initialContext)( pair => this.apply(pair))

    rewriters.foldLeft(rewrittenThat) {
      case ((context, t), r) => t.rewrite(context)(r)
    }
  }
}
