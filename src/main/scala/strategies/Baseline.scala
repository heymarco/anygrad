package strategies

import objects.bound.Hoeffding
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.Solution


class Baseline extends Strategy {
    val bound = new Hoeffding()
    val utility_function = new None()

    def name: String = {
        s"strategy-$m"
    }

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = m
}