/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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
package org.neo4j.cypher.internal.compiler.v2_0.ast

import org.neo4j.cypher.internal.compiler.v2_0._
import org.neo4j.cypher.internal.compiler.v2_0.symbols.CollectionType
import scala.Some
import org.neo4j.cypher.internal.compiler.v2_0.symbols.AnyType
import org.neo4j.cypher.internal.compiler.v2_0.ast.Expression.SemanticContext

sealed trait ReturnItems extends AstNode with SemanticCheckable {
  def declareIdentifiers(currentState: SemanticState): SemanticCheck
  
  def toCommands: (Seq[commands.ReturnColumn], Seq[commands.Unwind])
}

case class ListedReturnItems(items: Seq[ReturnItem], token: InputToken) extends ReturnItems {

  def declareIdentifiers(currentState: SemanticState) = {
    items.foldLeft(SemanticCheckResult.success)((sc, item) => item.alias match {
      case Some(identifier) => item.unwind match {
        case Some(unwind) => sc then identifier.declareWithIteratedType(item.expression)
        case None         => sc then identifier.declare(item.expression.types(currentState))
      }
      case None => sc
    })
  }

  def semanticCheck = items.foldLeft((SemanticCheckResult.success, Set.empty[String])) {
    (acc: (SemanticCheck, Set[String]), item: ReturnItem) => item.semanticCheckReferences(acc)
  }._1

  def toCommands = {
    val (itemCommands, optUnwindCommands) = items.map(_.toCommand).unzip
    (itemCommands, optUnwindCommands.flatten)
  }
}

case class ReturnAll(token: InputToken) extends ReturnItems {
  def semanticCheck = SemanticCheckResult.success

  def declareIdentifiers(currentState: SemanticState) = s => SemanticCheckResult.success(s.importSymbols(currentState.symbolTable))

  def toCommands = (Seq(commands.AllIdentifiers()), Seq.empty)
}


sealed trait ReturnItem extends AstNode {
  def expression: Expression
  def alias: Option[Identifier]
  def name: String

  def unwind: Option[Unwind]

  def semanticCheckReferences(in: (SemanticCheck, Set[String])): (SemanticCheck, Set[String]) = {
    val (priorCheck, aliasedIdentifiers) = in

    val (itemCommand, _) = toCommand
    val referenced = itemCommand.expression.symbolTableDependencies

    val newCheck = {
      val checkReferences = { (s: SemanticState) =>
        val foo = expression.semanticCheck(SemanticContext.Simple)(s).state
        val conflicting: Set[String] = aliasedIdentifiers.intersect(referenced)
        if (conflicting.isEmpty) {
          Right(s)
        } else {
          Left(SemanticError(
            s"Cannot reference previous return item ${conflicting.mkString(", ")} in same RETURN/WITH.",
            expression.token
          ))
        }
      }

      unwind match {
        case Some(unwind) => semanticCheckExpression then checkReferences then constrainToCollection
        case None         => semanticCheckExpression then checkReferences
      }
    }

    val newAliasedIdentifiers = if (alias.isDefined) {
      if (unwind.isDefined) {
        aliasedIdentifiers + name
      } else {
        expression match {
          case Identifier(name, _) => aliasedIdentifiers
          case _                   => aliasedIdentifiers + name
        }
      }
    } else {
      aliasedIdentifiers
    }


    (priorCheck then newCheck, newAliasedIdentifiers)
  }

  private def constrainToCollection = expression.constrainType(CollectionType(AnyType()))
  private def semanticCheckExpression = expression.semanticCheck(Expression.SemanticContext.Results)

  def toCommand: (commands.ReturnItem, Option[commands.Unwind])
}

case class UnaliasedReturnItem(unwind: Option[ast.Unwind], expression: Expression, token: InputToken)
  extends ReturnItem {

  val alias = expression match {
    case i: Identifier => Some(i)
    case _ => None
  }
  val name = alias.map(_.name) getOrElse { token.toString.trim }

  def toCommand: (commands.ReturnItem, Option[commands.Unwind]) = {
    val itemCommand = commands.ReturnItem(expression.toCommand, name)
    val unwindCommand = unwind.map(_.toCommand(itemCommand))
    (itemCommand, unwindCommand)
  }
}

case class AliasedReturnItem(unwind: Option[ast.Unwind], expression: Expression, identifier: Identifier, token: InputToken)
  extends ReturnItem {
  
  val alias = Some(identifier)
  val name = identifier.name

  def toCommand: (commands.ReturnItem, Option[commands.Unwind]) = {
    val itemCommand = commands.ReturnItem(expression.toCommand, name, renamed = true)
    val unwindCommand = unwind.map(_.toCommand(itemCommand))
    (itemCommand, unwindCommand)
  }
}
