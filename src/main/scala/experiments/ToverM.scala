package experiments

import scala.math.pow

import strategies.Baseline
import traits.Experiment
import traits.Repeatable


class ToverM extends Experiment {
    val m_list = Array(1, 2, 3, 4, 6, 8, 12, 15, 18, 21, 25, 30, 35, 40, 50)

    def init_strategies() = {
        strategies = Array[Repeatable]()
        for (m <- m_list) {
            val strategy = new Baseline()
            strategy.m = m
            strategies :+= strategy
        }
    }
}