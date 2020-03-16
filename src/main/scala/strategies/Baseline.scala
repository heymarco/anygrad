package strategies

import objects.bound.Hoeffding
import objects.estimators.MCDE
import traits.Strategy
import objects.utility.ValueLinear
import utils.types.Solution


class Baseline extends Strategy {
    val bound = new Hoeffding()
    val estimator = new MCDE()
    val utility_function = new ValueLinear()

    def name: String = {
        s"baseline-$m"
    }

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = m
}