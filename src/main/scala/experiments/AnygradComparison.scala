package experiments

import strategies.anygrad_variations.AnyGradSelectProbability
import strategies.{AnyGrad, AnyGradSelectAll, Baseline}
import traits.Experiment
import traits.Repeatable


class AnygradComparison extends Experiment {
    val m_list: Array[Int] = Array(10, 50, 100, 200)
    val name = "Comparison"

    def init_strategies() = {
        strategies = Array[Repeatable]()
        for (m <- m_list) {
            val strategy = new Baseline()
            strategy.m = m
            strategies :+= strategy
        }
        strategies :+= new AnyGradSelectAll
        strategies :+= new AnyGradSelectProbability
    }
}
