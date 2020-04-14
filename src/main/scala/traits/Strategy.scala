package traits

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import io.github.edouardfouche.utils.StopWatch
import objects.bound.Hoeffding
import traits.Bound
import traits.IterativeDependencyEstimator
import traits.Utility
import traits.Repeatable
import utils.helper.{default_snapshot, update_solution, wait_nonblocking}
import utils.types.{Snapshot, Solution}
import utils.MeasuresSwitchingTime


trait Strategy extends Repeatable {
    val estimator: IterativeDependencyEstimator
    val bound: Bound
    val utility_function: Utility
    var m: Int = 20
    protected var epsilon = 0.03
    protected var sleep = 0.0 // [ms]

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int

    def setup(args: Map[String, String]): Unit = {
        sleep = args.getOrElse("-s", "0.0").toDouble
        epsilon = args.getOrElse("-eps", "0.03").toDouble
    }

    def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)], results: Array[Array[Snapshot]]): ArrayBuffer[Int] = {
        targets.zipWithIndex
        .filter { case (target, index) =>
            val quality = results(target._1)(target._2)._2
            quality < until
        }
        .map(_._2)
    }

    def run(data: Array[Array[Double]], until: Double): Array[Array[Array[Snapshot]]] = { // Returns an array of matrices
        val targets = ArrayBuffer[(Int, Int)]()

        val results = ArrayBuffer[Array[Array[Snapshot]]]()

        val num_elements = data.size
        for {
            i <- 1 until num_elements
            j <- 0 until i
        }
        {
            targets.append((i, j))
        }
        val pdata = estimator.preprocess(data)
        // prevent cold start
        for (p <- targets) {
            val _ = estimator.run(pdata, Set(p._1, p._2), 1)
        }
        var M = 0
        var r = 0
        var t_cs = 1.0
        var t_1 = 1.0
        var active_targets = targets.zipWithIndex.map(_._2)
        val default_solution = (0.0, 0, (0, 0.0, 0.0))
        val T_start = StopWatch.stop()._1
        val timer = new MeasuresSwitchingTime()
        results.append(Array.fill[Snapshot](num_elements, num_elements)(default_snapshot(default_solution, bound=bound, eps=epsilon)))
        while (active_targets.size > 0) {
            val round_start = StopWatch.stop()._1
            val iterating_start = StopWatch.stop()._1
            for (i <- active_targets) {
                timer.track_start_time()
                val round_results = Array.ofDim[Snapshot](num_elements, num_elements)
                for (p <- targets) {
                    round_results(p._1)(p._2) = results.last(p._1)(p._2)
                    round_results(p._2)(p._1) = results.last(p._1)(p._2)
                }
                val p = targets(i)
                val current_result = round_results(p._1)(p._2)._1
                val iterations = if (r == 0) { m } else { get_m(current_result, t_cs, t_1) }
                val (dependency_update, time, variance) = estimator.run(pdata, Set(p._1, p._2), iterations)
                val result = (dependency_update, iterations, variance)
                val updated_result = update_solution(current_result, result)
                val T_now = StopWatch.stop()._1
                M = M + iterations
                val T = T_now - T_start
                val quality = 1 - bound.value(updated_result, epsilon)
                val utility = utility_function.compute(updated_result)
                wait_nonblocking(sleep)
                round_results(p._1)(p._2) = (updated_result, quality, utility, M, T)
                round_results(p._2)(p._1) = (updated_result, quality, utility, M, T)
                results.append(round_results)
                timer.track_end_time()
                val measurement = timer.calculate_switching_time(time, iterations)
                t_cs = measurement._1
                t_1 = measurement._2
            }
            val iterating_duration = StopWatch.stop()._1 - iterating_start
            val current_num_targets = active_targets.size
            active_targets = select_active_targets(until, targets = targets, results = results.last)
            r = r + 1
            val round_processing_time = StopWatch.stop()._1 - round_start - iterating_duration
            timer.track_round_processing_overhead(round_processing_time, current_num_targets)
        }
        val upper_triangle = ((0 until results.size).map(i => results(i).zipWithIndex.map{case(r,j) => r.drop(j+1)}.dropRight(1)).toArray)
        upper_triangle
    }
}