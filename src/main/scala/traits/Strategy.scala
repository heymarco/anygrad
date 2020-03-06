package traits

import scala.collection.mutable.ArrayBuffer
import io.github.edouardfouche.utils.StopWatch

import objects.bound.Hoeffding
import traits.Bound
import traits.IterativeDependencyEstimator
import traits.Utility
import traits.MeasuresSwitchingTime
import traits.Repeatable
import utils.helper.update_solution
import utils.types.Solution


trait Strategy extends Repeatable {
    val estimator: IterativeDependencyEstimator
    val bound: Bound
    val utility_function: Utility
    val epsilon = 0.03

    def get_m(t_cs: Double, t_1: Double, r: Int): Int

    def run(data: Array[Array[Double]], until: Double): Array[Array[Array[(Solution, Double, Double, Double)]]] = { // Returns an array of matrices
        val targets = ArrayBuffer[(Int, Int)]()

        val results = ArrayBuffer[Array[Array[(Solution, Double, Double, Double)]]]()

        val num_elements = data.size
        for {
            i <- 1 until num_elements
            j <- 0 until i
        }
        {
            targets.append((i, j))
        }
        val pdata = estimator.preprocess(data)
        var Q_avg = 0.0
        var M = 0
        val T_start = StopWatch.stop()._1
        var r = 0
        var t_cs = 0.1
        var t_1 = 0.9
        while (Q_avg < until) {
            val iterations = get_m(t_cs, t_1, r)
            var Q_sum = 0.0
            var round_results = Array.ofDim[(Solution, Double, Double, Double)](num_elements, num_elements)
            for (p <- targets) {
                val (dependency_update, time, variance) = estimator.run(pdata, Set(p._1, p._2), iterations)
                val result = (dependency_update, iterations, variance)
                val current_result = if (r == 0) (0.0, 0, (0, 0.0, 0.0)) else results.last(p._1)(p._2)._1
                val updated_result = update_solution(current_result, result)
                val T_now = StopWatch.stop()._1
                M = M + iterations
                val T = T_now - T_start
                val quality = 1 - bound.value(updated_result, epsilon)
                Q_sum = Q_sum + quality
                Q_avg = Q_sum / targets.size
                round_results(p._1)(p._2) = (updated_result, Q_avg, M, T)
                round_results(p._2)(p._1) = (updated_result, Q_avg, M, T)
            }
            results.append(round_results)
            r = r + 1
        }
        val upper_triangle = ((0 until results.size).map(i => results(i).zipWithIndex.map{case(r,j) => r.drop(j+1)}.dropRight(1)).toArray)
        upper_triangle
    }
}