package strategies.anygrad_variations

import traits.Strategy
import utils._Snapshot
import utils.types.{Snapshot, Solution}

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, sqrt}
import scala.util.Random.nextFloat


class AnyGradSelectProbability extends Strategy {
    var i = 0

    def name: String = "anygrad-sp"

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
                                       results: Array[Array[_Snapshot]]): Array[Int] = {
        val activeTargetIndices = super.select_active_targets(until, targets, results)
        if (activeTargetIndices.isEmpty) {
            return activeTargetIndices
        }
        var minGradient = Double.MaxValue
        var maxGradient = Double.MinValue
        val allGradients = activeTargetIndices.map(
            index => {
                val thisTarget = targets(index)
                val result = results(thisTarget._1)(thisTarget._2)
                val utility = result.getUtility()
                val gradient = -bound.dM(result.getSolution(), eps = epsilon) * utility
                i += 1
                minGradient = if (gradient < minGradient) gradient else minGradient
                maxGradient = if (gradient > maxGradient) gradient else maxGradient
                gradient
            })
        if (maxGradient == minGradient) { // All gradients are equal
            return activeTargetIndices
        }
        val filteredActiveTargetIndices = activeTargetIndices.zipWithIndex.filter {
            case (_, index) => {
                (allGradients(index) - minGradient)/(maxGradient - minGradient) >= nextFloat()
            }
        }.map(_._1)
        filteredActiveTargetIndices
    }
}