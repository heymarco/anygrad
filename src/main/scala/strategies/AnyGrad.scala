package strategies

import scala.collection.mutable.ArrayBuffer

import objects.bound.Hoeffding
import objects.estimators.MCDE
import traits.Strategy
import objects.utility.ValueLinear
import utils.types.Snapshot


class AnyGrad extends Strategy {
    var m = 5
    val bound = new Hoeffding()
    val estimator = new MCDE()
    val utility_function = new ValueLinear()

    def name: String = {
        s"anygrad"
    }

    //TODO: Implement correct function for calculating m_opt
    //      Maybe also do an ablation analysis
    def get_m(t_cs: Double, t_1: Double, r: Int): Int = {
        return m
    }

    override def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)], results: Array[Array[Snapshot]]): ArrayBuffer[(Int, Int)] = {
        val flattened_results = (results.zipWithIndex.map{case(r,j) => r.drop(j+1)}).flatten
        var dU_max = 0.0
        val dU_arr = flattened_results.map { case snapshot =>
            val utility = snapshot._3
            val dQ = -bound.dM(snapshot._1, eps = epsilon)
            val dU = utility * dQ
            if (dU > dU_max) {
                dU_max = dU
            }
            dU
        }
        var i = 0
        val active_targets = targets.filter { case _ =>
            var select = flattened_results(i)._2 < until
            if (select) {
                val dU_norm = dU_arr(i) / dU_max
                select = scala.util.Random.nextFloat() <= dU_norm
            }
            i += 1
            select
        }
        active_targets
    }
}