package strategies

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, pow, sqrt}
import objects.bound.{Chernoff, Hoeffding}
import objects.estimators.MCDE
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.{Snapshot, Solution}


class AnyGradSelectAll extends Strategy {
    var x: Double = 1.0

    def name: String = {
        s"anygrad-sa"
    }

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = {
        val A = -bound.dM(solution, eps = epsilon)
        val B = -0.5*bound.ddM(solution, eps = epsilon)
        val C = t_cs
        val D = t_1
        val m_opt = (-C + D*sqrt((C*(B*C - A*D))/(B*D*D)))/D
        val result = max(1, (m_opt * x).ceil.toInt)
        result
    }
}