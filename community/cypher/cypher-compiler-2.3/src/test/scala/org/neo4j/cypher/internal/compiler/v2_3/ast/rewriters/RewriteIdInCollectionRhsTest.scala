package org.neo4j.cypher.internal.compiler.v2_3.ast.rewriters

import org.neo4j.cypher.internal.compiler.v2_3.planner.AstRewritingTestSupport
import org.neo4j.cypher.internal.frontend.v2_3.ast._
import org.neo4j.cypher.internal.frontend.v2_3.test_helpers.CypherFunSuite

class RewriteIdInCollectionRhsTest extends CypherFunSuite with AstRewritingTestSupport {

  test("MATCH (a) WHERE id(a) IN [42, 43]") {
    shouldRewrite(
      "MATCH (a) WHERE id(a) IN [42, 43]",
      Query(None,SingleQuery(List(
        Match(optional = false,Pattern(List(EveryPath(NodePattern(Some(ident("a")),List(),None,naked = false)_)))_,List(),
          Some(Where(In(
            FunctionInvocation(FunctionName("id")_, distinct = false, Vector(ident("a")))_,
            Collection(List(
              IdentityId(SignedDecimalIntegerLiteral("42")_)_,
              IdentityId(SignedDecimalIntegerLiteral("43")_)_
            ))_
          )_)_))_
      ))_)_)
  }

  test("MATCH (a) WHERE id(a) IN ([42, 43, 44][1..2])") {
    shouldRewrite(
      "MATCH (a) WHERE id(a) IN ([42, 43, 44][1..2])",
      Query(None,SingleQuery(List(
        Match(optional = false,Pattern(List(EveryPath(NodePattern(Some(ident("a")),List(),None,naked = false)_)))_,List(),
          Some(Where(In(
            FunctionInvocation(FunctionName("id")_, distinct = false, Vector(ident("a")))_,
            CollectionSlice(
              Collection(List(
                IdentityId(SignedDecimalIntegerLiteral("42")_)_,
                IdentityId(SignedDecimalIntegerLiteral("43")_)_,
                IdentityId(SignedDecimalIntegerLiteral("44")_)_
              ))_,
              Some(SignedDecimalIntegerLiteral("1")_),
              Some(SignedDecimalIntegerLiteral("2")_)
            )_
          )_)_))_
      ))_)_)
  }

  test("MATCH (a) WHERE id(a) IN {param}") {
    shouldRewrite(
      "MATCH (a) WHERE id(a) IN {param}",
      Query(None,SingleQuery(List(
        Match(optional = false,Pattern(List(EveryPath(NodePattern(Some(ident("a")),List(),None,naked = false)_)))_,List(),
          Some(Where(In(
            FunctionInvocation(FunctionName("id")_, distinct = false, Vector(ident("a")))_,
            IdentityIds(Parameter("param")_)_
          )_)_))_
      ))_)_)
  }

  private def shouldRewrite(from: String, expect: ASTNode) {
    val original = parser.parse(from).asInstanceOf[Query]
    val actual = rewriteIdInCollectionRhs(original)

    actual should equal(expect)
  }
}
