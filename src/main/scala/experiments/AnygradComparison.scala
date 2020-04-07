package experiments

import scala.math.pow
import strategies.{Baseline, AnyGradSelectAll}
import traits.Experiment
import traits.Repeatable


class AnygradComparison extends Experiment {
    val m_list: Array[Int] = Array(4, 32, 128, 512)
    def init_strategies() = {
        strategies = Array[Repeatable]()
        for (m <- m_list) {
            val strategy = new Baseline()
            strategy.m = m
            strategies :+= strategy
        }
        val strategy = new AnyGradSelectAll()
        strategies :+= strategy
    }
}