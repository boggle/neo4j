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

  def semanticCheck = items.foldLeft(SemanticCheckResult.success)( (f, o) => f then o.semanticCheck )

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

  def semanticCheck: SemanticCheck =  {
    val hideIdentifier = (s: SemanticState) => {
      val nextState = alias match {
        case Some(alias_) if unwind.isDefined =>
          s.hideIdentifier(alias_)
        case Some(alias_) =>
          expression match {
            case Identifier(name, _) => s
            case _                   => s.hideIdentifier(alias_)
          }
        case _ =>
          s
      }

      Right(nextState)
    }

    unwind match {
      case Some(_) => (semanticCheckExpression ifOkThen constrainToCollection) then hideIdentifier
      case None    => semanticCheckExpression then hideIdentifier
    }
  }


  private def semanticCheckExpression: SemanticCheck = expression.semanticCheck(Expression.SemanticContext.Results)

  private def constrainToCollection = expression.constrainType(CollectionType(AnyType()))

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
