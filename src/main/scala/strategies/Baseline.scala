package strategies

import objects.bound.{Chernoff, Hoeffding}
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.Solution


class Baseline extends Strategy {

    def name: String = { s"baseline-$m"}

    override def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = m
}