package strategies

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, pow, sqrt}
import objects.bound.Hoeffding
import objects.estimators.MCDE
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.{Snapshot, Solution}


class AnyGradSelectAll extends Strategy {
    val bound = new Hoeffding()
    val estimator = new MCDE()
    val utility_function = new None()
    var x: Double = 1.0

    def name: String = {
        s"anygrad-sa-${x}"
    }

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = {
        val A = bound.dM(solution, eps = epsilon)
        val B = 0.5*bound.ddM(solution, eps = epsilon)
        val C = t_cs
        val D = t_1
        val m_opt = (B * C + sqrt(B*B*C*C-A*B*C*D))/(B*D)
        max(1, (m_opt * x).toInt)
    }
}