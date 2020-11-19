package strategies

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, pow, sqrt}
import objects.bound.{Chernoff, Hoeffding}
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.{Snapshot, Solution}


class AnyGrad extends Strategy {
    var i = 0

    def name: String = "anygrad"

    /**
     * Select the active targets
     *
     * Combination of UCB-sampling and filtering of targets where quality is less than `until`
     *
     * @param until   The minimum quality of the target
     * @param targets All targets
     * @param results The results (snapshots) obtained so far
     * @return Indices of the active targets
     */
    override def select_active_targets(until: Double,
                                       targets: ArrayBuffer[(Int, Int)],
                                       results: Array[Array[Snapshot]]): Array[Int] = {
        val activeTargetIndices = super.select_active_targets(until, targets, results)
        if (activeTargetIndices.isEmpty) {
            return activeTargetIndices
        }
        val numActiveTargets = activeTargetIndices.length
        var minGradient = Double.MaxValue
        var maxGradient = Double.MinValue
        // println(String.format("data_%s = [", i))
        val allGradients = activeTargetIndices.map(
            index => {
                val thisTarget = targets(index)
                val result = results(thisTarget._1)(thisTarget._2)
                val utility = result._3
                val gradient = -bound.dM(result._1, eps = epsilon) * utility
                // println(String.format("%s, ", i))
                i += 1
                minGradient = if (gradient < minGradient) { gradient } else { minGradient }
                maxGradient = if (gradient > maxGradient) { gradient } else { maxGradient }
                gradient
            })
        // println("]")
        val filteredActiveTargetIndices = activeTargetIndices.zipWithIndex.filter {
            case (target, index) => {
                allGradients(index) - minGradient >= (maxGradient - minGradient) / 2.0
            }
        }.map(_._1)
        // println(String.format("%s, ", filteredActiveTargetIndices.length.toDouble/numActiveTargets))
        filteredActiveTargetIndices
    }
}