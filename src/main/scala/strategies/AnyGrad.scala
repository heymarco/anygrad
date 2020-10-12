package strategies

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, pow, sqrt}
import objects.bound.{Chernoff, Hoeffding}
import objects.estimators.MCDE
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.{Snapshot, Solution}


class AnyGrad extends Strategy {
    val bound = new Chernoff()
    val utility_function = new None()
    var x: Double = 1.0

    def name: String = {
        s"anygrad"
    }

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = {
        val A = -bound.dM(solution, eps = epsilon)
        val B = -0.5*bound.ddM(solution, eps = epsilon)
        val C = t_cs
        val D = t_1
        val m_opt = (sqrt(B*B*C*C - A*B*C*D) - B*C)/(B*D)
        val result = max(1, (m_opt * x).toInt)
        result
    }

    // override def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)], results: Array[Array[Snapshot]]): ArrayBuffer[Int] = {
    //     var dU_max = 0.0
    //     val dU_arr = flattened_results.map { case snapshot =>
    //         val utility = snapshot._3
    //         val dQ = -bound.dM(snapshot._1, eps = epsilon)
    //         val dU = utility * dQ
    //         if (dU > dU_max) {
    //             dU_max = dU
    //         }
    //         dU
    //     }
    //     var i = 0
    //     val active_targets = targets.filter { case _ =>
    //         var select = flattened_results(i)._2 < until
    //         if (select) {
    //             val dU_norm = dU_arr(i) / dU_max
    //             select = scala.util.Random.nextFloat() <= dU_norm
    //         }
    //         i += 1
    //         select
    //     }
    //     active_targets
    // }
}