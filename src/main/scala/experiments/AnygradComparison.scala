package experiments

import strategies.{AnyGradSelectAll, AnyGrad, Baseline}
import traits.Experiment
import traits.Repeatable


class AnygradComparison extends Experiment {
    val m_list: Array[Int] = Array(2, 10, 50, 100)
    def init_strategies() = {
        strategies = Array[Repeatable]()
        for (m <- m_list) {
            val strategy = new Baseline()
            strategy.m = m
            strategies :+= strategy
        }
        val anygrad_sa = new AnyGradSelectAll()
        strategies :+= anygrad_sa
        val anygrad = new AnyGrad()
        strategies :+= anygrad
    }
}
