package traits

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import io.github.edouardfouche.utils.StopWatch
import io.github.edouardfouche.index.Index
import objects.estimators.MCDE
import objects.bound.{Chernoff, Hoeffding}
import objects.utility.{Identity, None}
import traits.Bound
import traits.IterativeDependencyEstimator
import traits.Utility
import traits.Repeatable
import utils.helper.{default_snapshot, update_solution, wait_nonblocking}
import utils.types.{Snapshot, Solution}
import utils.MeasuresSwitchingTime


trait Strategy extends Repeatable {

    var estimators = Array[IterativeDependencyEstimator]()
    val bound: Bound = new Hoeffding()
    val utility_function: Utility = new Identity()
    var m: Int = 10
    protected var epsilon = 0.03
    protected var sleep = 0.0 // [ms]

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int

    def setup(args: Map[String, String]): Unit = {
        sleep = args.getOrElse("-s", "0.0").toDouble
        epsilon = args.getOrElse("-eps", "0.03").toDouble
    }

    def init_dependency_estimators(num_targets: Int) {
        for (_ <- 0 to num_targets) {
            estimators = estimators :+ new MCDE()
        }
    }

    def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)], results: Array[Array[Snapshot]]): ArrayBuffer[Int] = {
        targets.zipWithIndex.filter(item => {
            val quality = results(item._1._1)(item._1._2)._2
            quality < until
        }).map(_._2)
    }

    def run(data: Array[Array[Double]], until: Double): Array[Array[Array[Snapshot]]] = { // Returns an array of matrices
        var targets = ArrayBuffer[(Int, Int)]()
        val results = ArrayBuffer[Array[Array[Snapshot]]]()

        val num_elements = data.length
        for {
            i <- 1 until num_elements
            j <- 0 until i
        }
        {
            targets.append((i, j))
        }
        init_dependency_estimators(targets.size)
        
        val pdata = estimators(0).preprocess(data)

        // prevent cold start
        for ((p, i) <- targets.zipWithIndex) {
            val _ = estimators(i).run(pdata, Set(p._1, p._2), 10)
        }
        print(targets)
        var active_targets = ArrayBuffer[Int]()
        var totalIterations = 0
        var r = 0
        var t_cs = Double.NaN
        var t_1 = Double.NaN
        val default_solution: Solution = (0.0, 0, (0, 0.0, 0.0))
        val T_start = StopWatch.stop()._1
        var T_prev = 0.0
        val timer = new MeasuresSwitchingTime()
        active_targets = targets.indices.to(ArrayBuffer)
        results.append(Array.fill[Snapshot](num_elements, num_elements)(default_snapshot(default_solution, bound=bound, eps=epsilon)))
        while (active_targets.nonEmpty) {
            val round_start = StopWatch.stop()._1
            val iterating_start = StopWatch.stop()._1
            val round_results = Array.ofDim[Snapshot](num_elements, num_elements)
            for (p <- targets) {
                round_results(p._1)(p._2) = results.last(p._1)(p._2)
                round_results(p._2)(p._1) = results.last(p._1)(p._2)
            }
            for (i <- active_targets) {
                timer.track_start_time()
                val p = targets(i)
                val current_result = round_results(p._1)(p._2)._1
                val iterations = if (r == 0) { m } else { get_m(current_result, t_cs, t_1) }
                val (dependency_update, time, variance) = estimators(i).run(pdata, Set(p._1, p._2), iterations)
                val result = (dependency_update, iterations, variance)
                val updated_result = update_solution(current_result, result)
                totalIterations = totalIterations + iterations
                val T = StopWatch.stop()._1 - T_start
                val quality = 1 - bound.value(updated_result, epsilon)
                val utility = utility_function.compute(updated_result)
                wait_nonblocking(sleep)
                // (solution, quality, utility, iterations, time, m, t_cs, t_1)
                val new_snapshot: Snapshot = (updated_result, quality, utility, totalIterations, T, iterations, t_cs, t_1)
                round_results(p._1)(p._2) = new_snapshot
                round_results(p._2)(p._1) = new_snapshot
                timer.track_end_time()
                val measurement = timer.calculate_switching_time(time, iterations)
                t_cs = measurement._1
                t_1 = measurement._2
                T_prev = T
            }
            results.append(round_results)
            val iterating_duration = StopWatch.stop()._1 - iterating_start
            r = r + 1
            val round_processing_time = StopWatch.stop()._1 - round_start - iterating_duration
            timer.track_round_processing_overhead(round_processing_time, active_targets.size)
            active_targets = select_active_targets(until, targets = targets, results = results.last)
        }
        println(String.format("Total runtime = %s", StopWatch.stop()._1 - T_start))
        val upper_triangle = results.indices
            .map(i => results(i)
                .zipWithIndex
                .map{case(r,j) => r.drop(j+1)}
                .dropRight(1)).toArray
        upper_triangle
    }
}