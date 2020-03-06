package strategies

import objects.bound.Hoeffding
import objects.estimators.MCDE
import traits.Strategy
import objects.utility.ValueLinear


class Baseline extends Strategy {
    var m = 5
    val bound = new Hoeffding()
    val estimator = new MCDE()
    val utility_function = new ValueLinear()

    def name: String = {
        s"baseline-$m"
    }

    def get_m(t_cs: Double, t_1: Double, r: Int): Int = {
        return m
    }
}