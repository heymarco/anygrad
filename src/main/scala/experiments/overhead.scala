package experiments

import scala.math.pow

import strategies.Baseline
import traits.Experiment
import traits.Repeatable


class TOverM extends Experiment {
    val num_data_points = 10
    val step_size = 2
    val start = 1

    val N = 1

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