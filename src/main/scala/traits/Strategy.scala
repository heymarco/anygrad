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
    var m: Int = 5
    protected var epsilon = 0.03
    protected var sleep = 0.0 // [ms]

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int

    def setup(args: Map[String, String]): Unit = {
        sleep = args.getOrElse("-s", "0.0").toDouble
        epsilon = args.getOrElse("-eps", "0.03").toDouble
    }

    def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)], results: Array[Array[Snapshot]]): ArrayBuffer[(Int, Int)] = {
        val flattened_results = (results.zipWithIndex.map{case(r,j) => r.drop(j+1)}).flatten
        var i = 0
        val num_rows = results.size
        val active_targets = targets.filter { case _ =>
            val select = flattened_results(i)._2 < until
            i += 1
            select
        }
        active_targets
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
        var Q_avg = 0.0
        var M = 0
        var r = 0
        var t_cs = 1.0
        var t_1 = 1.0
        var active_targets = targets
        val default_solution = (0.0, 0, (0, 0.0, 0.0))
        val round_results = Array.fill[Snapshot](num_elements, num_elements)(default_snapshot(default_solution, bound=bound, eps=epsilon))
        val T_start = StopWatch.stop()._1
        val timer = new MeasuresSwitchingTime()
        while (active_targets.size > 0) {
            var Q_sum = 0.0
            var m_round = 0
            println("round")
            for (p <- active_targets) {
                timer.track_start_time()
                val current_result = if (r == 0) default_solution else round_results(p._1)(p._2)._1
                val iterations = if (r == 0) { m } else { get_m(current_result, t_cs, t_1) }
                val (dependency_update, time, variance) = estimator.run(pdata, Set(p._1, p._2), iterations)
                val result = (dependency_update, iterations, variance)
                val updated_result = update_solution(current_result, result)
                val T_now = StopWatch.stop()._1
                M = M + iterations
                val T = T_now - T_start
                val quality = 1 - bound.value(updated_result, epsilon)
                val utility = utility_function.compute(updated_result)
                Q_sum = Q_sum + quality
                Q_avg = Q_sum / targets.size
                round_results(p._1)(p._2) = (updated_result, quality, utility, M, T)
                round_results(p._2)(p._1) = (updated_result, quality, utility, M, T)
                m_round += iterations
                wait_nonblocking(sleep)
                val copy = round_results.map(arr => arr.clone)
                results.append(copy)
                timer.track_end_time()
                val measurement = timer.calculate_switching_time(time, iterations)
                t_cs = measurement._1
                t_1 = measurement._2
            }
            active_targets = select_active_targets(until, targets = targets, results = round_results)
            r = r + 1
        }
        val upper_triangle = ((0 until results.size).map(i => results(i).zipWithIndex.map{case(r,j) => r.drop(j+1)}.dropRight(1)).toArray)
        upper_triangle
    }
}