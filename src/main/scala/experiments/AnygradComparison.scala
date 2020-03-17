package experiments

import scala.math.pow
import strategies.AnyGrad
import traits.Experiment
import traits.Repeatable


class AnygradComparison extends Experiment {
    val x_list: Array[Double] = Array(0.2, 0.5, 1.0, 1.5, 2.5, 4)
    def init_strategies() = {
        strategies = Array[Repeatable]()
        for (x <- x_list) {
            val strategy = new AnyGrad()
            strategy.x = x
            strategies :+= strategy
        }
    }
}