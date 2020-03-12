package experiments

import scala.math.pow

import strategies.Baseline
import traits.Experiment
import traits.Repeatable


class ToverM extends Experiment {
    val num_data_points = 12
    val step_size = 1.5
    val start = 1

    def init_strategies() = {
        strategies = Array[Repeatable]()
        for (i <- 0 until num_data_points) {
            val m = (start * pow(step_size, i)).ceil.toInt
            val strategy = new Baseline()
            strategy.m = m
            strategies :+= strategy
        }
    }
}