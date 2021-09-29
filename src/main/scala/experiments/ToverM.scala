package experiments

import scala.math.pow

import strategies.Baseline
import traits.Experiment
import traits.Repeatable


class ToverM extends Experiment {
    val m_list = Array(2, 5, 10, 20, 30, 40, 60, 80, 100, 130, 160, 200)
    val name = "T over M"

    def init_strategies() = {
        strategies = Array[Repeatable]()
        for (m <- m_list) {
            val strategy = new Baseline()
            strategy.m = m
            strategies :+= strategy
        }
    }
}