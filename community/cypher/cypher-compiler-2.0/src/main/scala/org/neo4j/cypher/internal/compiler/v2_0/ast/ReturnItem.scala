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
import scala.Some
import org.neo4j.cypher.internal.compiler.v2_0.symbols.{AnyType, CollectionType}

sealed trait ReturnItems extends AstNode with SemanticCheckable {
  def toCommands : Seq[commands.ReturnColumn]
  def toUnwindCommands: Seq[commands.Unwind]

  def declareIdentifiers(currentState: SemanticState) : SemanticCheck
}

case class ListedReturnItems(items: Seq[ReturnItem], token: InputToken) extends ReturnItems {
  def semanticCheck = items.semanticCheck

  def declareIdentifiers(currentState: SemanticState) = {
    items.foldLeft(SemanticCheckResult.success)((sc, item) => item.alias match {
      case Some(identifier) => sc then identifier.declare(item.expression.types(currentState))
      case None => sc
    })
  }

  def toCommands = items.map(_.toCommand)
  def toUnwindCommands = items.flatMap(_.toUnwindCommand)
}

case class ReturnAll(token: InputToken) extends ReturnItems {
  def semanticCheck = SemanticCheckResult.success

  def declareIdentifiers(currentState: SemanticState) = s => SemanticCheckResult.success(s.importSymbols(currentState.symbolTable))

  def toCommands = Seq(commands.AllIdentifiers())
  def toUnwindCommands: Seq[commands.Unwind] = Seq.empty
}


sealed trait ReturnItem extends AstNode with SemanticCheckable {
  def expression: Expression
  def alias: Option[Identifier]
  def name: String

  def optUnwind: Option[ast.Unwind]

  def semanticCheck = optUnwind match {
    case Some(unwind) =>
      semanticCheckExpression then
      expression.constrainType(CollectionType(AnyType())) ifOkThen
      expression.unwindType()
    case None =>
      semanticCheckExpression
  }

  private def semanticCheckExpression = expression.semanticCheck(Expression.SemanticContext.Results)

  def toCommand: commands.ReturnItem
  def toUnwindCommand: Option[commands.Unwind]
}

case class UnaliasedReturnItem(optUnwind: Option[ast.Unwind], expression: Expression, token: InputToken) extends ReturnItem {
  val alias = expression match {
    case i: Identifier => Some(i)
    case _ => None
  }
  val name = alias.map(_.name) getOrElse { token.toString.trim }

  def toCommand = commands.ReturnItem(expression.toCommand, name)
  def toUnwindCommand = optUnwind.map(_.toUnwindCommand(toCommand))
}

case class AliasedReturnItem(optUnwind: Option[ast.Unwind], expression: Expression, identifier: Identifier, token: InputToken) extends ReturnItem {
  val alias = Some(identifier)
  val name = identifier.name

  def toCommand = commands.ReturnItem(expression.toCommand, name, renamed = true)
  def toUnwindCommand = optUnwind.map(_.toUnwindCommand(toCommand))
}
