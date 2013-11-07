package org.neo4j.cypher.internal.compiler.v2_0.prettifier

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object TokenCollector {
  val space = ' '
  val newline = System.getProperty("line.separator")
}

trait TokenCollector {
  def +=( token: SyntaxToken, break: Break)
  def result: String
}

sealed abstract class Break
case object NoBreak extends Break { override def toString = "" }
case object Blank extends Break { override def toString = " " }
case object NewLine extends Break { override def toString = TokenCollector.newline }

class StringTokenCollector extends TokenCollector {
  val builder = new StringBuilder
  def +=(token: SyntaxToken, break: Break): Unit = builder.append(token.toString).append(break.toString)
  def result: String = builder.toString()
}

class BreakingStringTokenCollector(lineWidth: Int, indentWidth: Int) extends TokenCollector {
  val breaks = mutable.ArrayBuffer.newBuilder[Break]
  val texts = mutable.ArrayBuffer.newBuilder[String]

  breaks += NoBreak

  var lineCount = 0
  val indent = 1.to(indentWidth).map(_ => " ").reduce(_+_)

  def +=(token: SyntaxToken, break: Break): Unit = {
    texts += token.toString
    breaks += break
  }

  def append(builder: StringBuilder, break: Break, text: String)  {
    val breakText = break.toString

    if (lineCount + breakText.length + text.length > lineWidth) {
      builder.append(TokenCollector.newline)
      appendIndented(builder, text)
      lineCount = 0
    } else {
      builder
        .append(breakText)
        .append(text)
      lineCount +=  breakText.length + text.length
    }
  }

  def appendIndented(builder: StringBuilder, text: String) {
    builder
      .append(indent)
      .append(text)
  }

  def result: String = {

    // TODO first fuse, then turn around

    // regroup parts with breaks first
    val initialParts: ArrayBuffer[(String, Break)] = texts.result().zip(breaks.result())

    // fuse NoBreak parts together
    val builder = new StringBuilder
    val parts = mutable.ArrayBuffer.newBuilder[(Break, String)]
    var previousBreak: Break = NewLine
    var previousText: String = ""
    for ( (text, break) <- initialParts if text.length > 0 ) {
      (previousBreak, break) match {
        case (NoBreak, NoBreak) =>
          previousText += text

        case (_, NoBreak) =>
          previousBreak = NoBreak
          previousText  = text

        case _ =>
          if (previousText.length > 0) {
            parts += ((NoBreak, previousText))
            previousText = ""
          }
          previousBreak = break
          parts += ((break, text))
      }
    }

    if (previousText.length > 0) {
      parts += ((previousBreak, previousText))
    }

    // render parts, potentially indenting overspilling lines
    val allParts: ArrayBuffer[(Break, String)] = parts.result()
    for ( (break, text) <- allParts )
      append(builder, break, text)

    builder.toString()
  }
}


