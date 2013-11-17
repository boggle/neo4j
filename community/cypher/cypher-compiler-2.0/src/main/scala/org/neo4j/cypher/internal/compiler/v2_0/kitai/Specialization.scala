package org.neo4j.cypher.internal.compiler.v2_0.kitai

import scala.reflect.runtime.universe._

object Specialization {
  
  val cypherTypes = new Specializable.Group((Char, Int, Long, Double, Boolean, AnyRef))
  
  val elementaryCypherTypeTags: Seq[TypeTag[_]] =
    Seq(typeTag[Char], typeTag[Int], typeTag[Long], typeTag[Double], typeTag[Boolean])
  
  def apply(t: TypeTag[_]): TypeTag[_] = elementaryCypherTypeTags.find( _.tpe =:= t.tpe ).getOrElse(typeTag[AnyRef])
}