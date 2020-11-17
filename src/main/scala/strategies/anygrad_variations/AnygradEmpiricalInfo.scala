package strategies.anygrad_variations

import io.github.edouardfouche.utils.StopWatch
import traits.Strategy
import utils.{MeasuresSwitchingTime, PerformanceObserver}
import utils.helper.{default_snapshot, update_solution, wait_nonblocking}
import utils.types.{Snapshot, Solution}

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, sqrt}
import scala.util.Random.nextFloat


class AnygradEmpiricalInfo extends Strategy {
    private var performanceObserver: PerformanceObserver = null

    def name: String = {
        s"anygrad-ei"
    }

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
                                       results: Array[Array[Snapshot]],
                                       roundOverhead: Double): Array[Int] = {
        val activeTargetIndices = super.select_active_targets(until, targets, results, roundOverhead = roundOverhead)
        if (activeTargetIndices.isEmpty) {
            return activeTargetIndices
        }
        var minGradient = Double.MaxValue
        var maxGradient = Double.MinValue
        val allGradients = activeTargetIndices.map(
            index => {
                val thisTarget = targets(index)
                val result = results(thisTarget._1)(thisTarget._2)
                val utility = result._3
                val qualityGradient = performanceObserver.get1stDerivationApproximation(forTarget = index)
                val utilityGradient = qualityGradient * utility
                minGradient = if (utilityGradient < minGradient) { utilityGradient } else { minGradient }
                maxGradient = if (utilityGradient > maxGradient) { utilityGradient } else { maxGradient }
                utilityGradient
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

    override def run(data: Array[Array[Double]], until: Double): Array[Array[Array[Snapshot]]] = { // Returns an array of matrices
        val targets = ArrayBuffer[(Int, Int)]()
        val results = ArrayBuffer[Array[Array[Snapshot]]]()

        val num_elements = data.length
        for {
            i <- 1 until num_elements
            j <- 0 until i
        } {
            targets.append((i, j))
        }
        performanceObserver = new PerformanceObserver(numTargets = targets.length)
        init_dependency_estimators(targets.size)

        val pdata = estimators(0).preprocess(data)

        // prevent cold start
        for ((p, i) <- targets.zipWithIndex) {
            val _ = estimators(i).run(pdata, Set(p._1, p._2), 10)
        }
        var active_targets = Array[Int]()
        var totalIterations = 0
        var r = 0
        var t_cs = Double.NaN
        var t_1 = Double.NaN
        val default_solution: Solution = (0.0, 0, (0, 0.0, 0.0))
        val T_start = StopWatch.stop()._1
        val timer = new MeasuresSwitchingTime()
        var totalRoundOverhead = 0.0
        var round_timestamp = StopWatch.stop()._1
        active_targets = targets.indices.toArray
        results.append(
            Array.fill[Snapshot](num_elements, num_elements)(default_snapshot(default_solution, bound=bound, eps=epsilon))
        )
        while (active_targets.nonEmpty) {
            val iterating_start = StopWatch.stop()._1
            val round_results = results.last.map(_.clone())
            for (i <- active_targets) {
                timer.track_start_time()
                val p = targets(i)
                val current_result = round_results(p._1)(p._2)._1
                val iterations = if (burnInPhaseFinished(r)) { get_m(performanceObserver, i, t_cs, t_1) } else { m }
                val (dependency_update, time, variance) = estimators(i).run(pdata, Set(p._1, p._2), iterations)
                val result = (dependency_update, iterations, variance)
                val updated_result = update_solution(current_result, result)
                totalIterations = totalIterations + iterations
                val T = StopWatch.stop()._1 - T_start
                val quality = 1 - bound.value(updated_result, epsilon)
                performanceObserver.enqueue((updated_result._2, quality), at = i)
                val utility = utility_function.compute(updated_result)
                wait_nonblocking(sleep)
                timer.track_end_time()
                val measurement = timer.calculate_switching_time(time, iterations)
                t_cs = measurement._1
                t_1 = measurement._2
                val new_snapshot: Snapshot = (
                    updated_result, quality, utility,
                    totalIterations, T, iterations, t_cs, t_1
                )
                round_results(p._1)(p._2) = new_snapshot
                round_results(p._2)(p._1) = new_snapshot
            }
            val iterating_duration = StopWatch.stop()._1 - iterating_start
            results.append(round_results)
            val round_processing_time = StopWatch.stop()._1 - round_timestamp - iterating_duration
            round_timestamp = StopWatch.stop()._1
            timer.track_round_processing_overhead(round_processing_time, active_targets.size)
            r = r + 1
            if (burnInPhaseFinished(r)) {
                active_targets = select_active_targets(
                    until = until,
                    targets = targets,
                    results = results.last,
                    roundOverhead = timer.getTotalRoundOverhead()
                )
            }
            totalRoundOverhead += timer.getRoundOverheadPerTarget()
        }
        printStatistics(StopWatch.stop()._1 - T_start, totalRoundOverhead)
        getUpperTriangle(results)
    }
}
