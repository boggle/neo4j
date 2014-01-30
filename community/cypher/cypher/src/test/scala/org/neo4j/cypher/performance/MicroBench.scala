package org.neo4j.cypher.performance

abstract class MicroBench(val name: String, val warmUpRuns: Int, val chopRuns: Int, val measuredRuns: Int) {

  def setup() {}
  def shutdown() {}
  
  def run(): Unit

  def apply(): MicroStats = {
    val collector = Seq.newBuilder[Double]
    setup()
    try {
      (0 until warmUpRuns) foreach (_ => run() )
      (0 until measuredRuns) foreach { _ =>
        val start = System.nanoTime()
        run()
        val end = System.nanoTime()
        collector += (end - start) / 1000000
      }
      MicroStats(collector.result().sorted.drop(chopRuns).dropRight(chopRuns))
    } finally {
      shutdown()
    }
  }

  override def toString = name
}

object MicroStats {
  def apply(values: Seq[Double]): MicroStats = {
    val count = values.size
    var min = Double.MaxValue
    var max = Double.MinValue
    var sum = 0.0
    for ( v <- values ) {
      if ( v < min ) {
        min = v
      }
      if ( v > max ) {
        max = v
      }
      sum += v
    }
    val avg = sum / count

    var sqSum = 0.0
    for ( v <- values ) {
      val diff = v - avg
      sqSum += diff * diff
    }
    val variance = sqSum / count

    MicroStats(min, max, avg, Math.sqrt(variance), count)
  }
}

final case class MicroStats(min: Double, max: Double, avg: Double, dev: Double, count: Int)
