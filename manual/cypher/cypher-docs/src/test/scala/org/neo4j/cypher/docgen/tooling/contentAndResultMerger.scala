/*
 * Copyright (c) 2002-2015 "Neo Technology,"
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
package org.neo4j.cypher.docgen.tooling

import org.neo4j.cypher.internal.frontend.v2_3.Rewritable._
import org.neo4j.cypher.internal.frontend.v2_3.{bottomUp, Rewriter}

/**
 * Takes the document tree and the execution result and rewrites the
 * tree to include the result content
 */
object contentAndResultMerger {
  def apply(originalContent: Document, result: TestRunResult): Document = {
    val rewritesToDo = for {
      runResult <- result.queryResults
      newContent <- runResult.newContent
      original = runResult.original
    } yield original -> newContent

    val rewriter = new Rewriter {
      override def apply(value: AnyRef): AnyRef = instance(value)

      private val queryResultMap = rewritesToDo.toMap
      val instance = bottomUp(Rewriter.lift {
        case q: Query if queryResultMap.contains(q) => q.copy(content = queryResultMap(q))
        case q: GraphVizBefore if queryResultMap.contains(q) => queryResultMap(q)
      })
    }

    originalContent.endoRewrite(rewriter)
  }
}
