package org.neo4j.cypher.internal.compiler.v2_0.kitai

object Specialization {
  val cypherTypes = new Specializable.Group((Char, Int, Long, Double, Boolean, AnyRef))
}