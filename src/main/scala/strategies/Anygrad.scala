package strategies

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, pow, sqrt}
import objects.bound.{Chernoff, Hoeffding}
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.{Snapshot, Solution}


class Anygrad extends Strategy {
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

    /**
     * Select the active targets
     *
     * Combination of UCB-sampling and filtering of targets where quality is less than `until`
     * @param until The minimum quality of the target
     * @param targets All targets
     * @param results The results (snapshots) obtained so far
     * @return Indices of the active targets
     */
    override def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)],
                                       results: Array[Array[Snapshot]]): ArrayBuffer[Int] = {
        val activeTargetIndices = super.select_active_targets(until, targets, results)
        if (activeTargetIndices.isEmpty) {
            return activeTargetIndices
        }
        var best_val = (-1.0, -1)
        activeTargetIndices.foreach(index => {
            val thisTarget = targets(index)
            val result = results(thisTarget._1)(thisTarget._2)
            val conf = bound.confidence(result._1, delta = 1-until)
            val optimisticSolution = (result._1._1 + conf, result._1._2, result._1._3)
            val optimisticUtility = utility_function.compute(optimisticSolution)
            var slope = bound.dM(result._1, eps = epsilon)
            slope *= optimisticUtility
            best_val = if (optimisticUtility > best_val._1) { (optimisticUtility, index) } else { best_val }
        })
        val result = ArrayBuffer(best_val._2)
        result
    }
}