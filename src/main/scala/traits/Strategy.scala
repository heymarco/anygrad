package traits

import scala.collection.mutable.ArrayBuffer
import io.github.edouardfouche.utils.StopWatch
import objects.estimators.MCDE
import objects.bound.{Chernoff, Hoeffding}
import objects.utility.{Identity, None}
import utils.helper.{default_snapshot, update_solution, wait_nonblocking}
import utils.types.{Snapshot, Solution}
import utils.{MeasuresSwitchingTime, PerformanceObserver}

import scala.math.{max, sqrt}


trait Strategy extends Repeatable {
    var x: Double = 1.0  // TODO: What is x?
    var estimators = Array[IterativeDependencyEstimator]()
    val bound: Bound = new Hoeffding()
    val utility_function: Utility = new None()
    var m: Int = 10
    protected var epsilon = 0.03
    protected var sleep = 0.0 // [ms]
    protected val burnInPhaseLength = 10

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = {
        val dM = -bound.dM(solution, eps = epsilon)
        val ddM = -bound.ddM(solution, eps = epsilon)
//        println("***")
//        println("ratio gradient  = %s".format(dM/ddM))
//        println("ratio time  = %s".format(t_cs/t_1))
        get_m(dM, ddM, t_cs, t_1)
    }

    def get_m(performanceObserver: PerformanceObserver, targetIndex: Int,
              t_cs: Double, t_1: Double): Int = {
        val dM = performanceObserver.get1stDerivationApproximation(targetIndex)
        val ddM = performanceObserver.get2ndDerivationApproximation(targetIndex)
//        println("***")
//        println("ratio gradient  = %s".format(dM/ddM))
//        println("ratio time  = %s".format(t_cs/t_1))
        get_m(dM, ddM, t_cs, t_1)
    }

    def get_m(dM: Double, ddM: Double, t_cs: Double, t_1: Double): Int = {
        val A = dM
        val B = 0.5*ddM
        val C = t_cs
        val D = t_1
        val m_opt = (-C + D*sqrt((C*(B*C - A*D))/(B*D*D)))/D
        val result = max(1, (m_opt * x).ceil.toInt)
        result
    }

    def setup(args: Map[String, String]): Unit = {
        sleep = args.getOrElse("-s", "0.0").toDouble
        epsilon = args.getOrElse("-eps", "0.03").toDouble
    }

    def init_dependency_estimators(num_targets: Int) {
        for (_ <- 0 to num_targets) {
            estimators = estimators :+ new MCDE()
        }
    }

    def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)], results: Array[Array[((Double, Int, (Int, Double, Double)), Double, Double, Double, Double, Double, Double, Double)]], roundOverhead: Double): Array[Int] = {
        targets.zipWithIndex.filter(item => {
            val quality = results(item._1._1)(item._1._2)._2
            quality < until
        }).map(_._2).toArray
    }

    def run(data: Array[Array[Double]], until: Double): Array[Array[Array[Snapshot]]] = { // Returns an array of matrices
        val targets = ArrayBuffer[(Int, Int)]()
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
                val iterations = if (burnInPhaseFinished(r)) { get_m(current_result, t_cs, t_1) } else { m }
                val (dependency_update, time, variance) = estimators(i).run(pdata, Set(p._1, p._2), iterations)
                val result = (dependency_update, iterations, variance)
                val updated_result = update_solution(current_result, result)
                totalIterations = totalIterations + iterations
                val T = StopWatch.stop()._1 - T_start
                val quality = 1 - bound.value(updated_result, epsilon)
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
                active_targets = select_active_targets(until,
                    targets = targets,
                    results = results.last,
                    roundOverhead = timer.getTotalRoundOverhead())
            }
            totalRoundOverhead += timer.getRoundOverheadPerTarget()
        }
        printStatistics(StopWatch.stop()._1 - T_start, totalRoundOverhead)
        getUpperTriangle(results)
    }

    protected def getUpperTriangle(results: ArrayBuffer[Array[Array[Snapshot]]]): Array[Array[Array[Snapshot]]] = {
        results.indices
            .map(i => results(i)
                .zipWithIndex
                .map{case(r,j) => r.drop(j+1)}
                .dropRight(1))
            .toArray
    }

    protected def burnInPhaseFinished(roundIndex: Int): Boolean = roundIndex >= burnInPhaseLength

    protected def printStatistics(totalRuntime: Double, totalRoundOverhead: Double): Unit = {
        println("Total runtime of %s = %s".format(name, totalRuntime))
        println("Total round overhead of %s = %s".format(name, totalRoundOverhead))
    }
}