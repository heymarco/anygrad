package traits

import scala.collection.mutable.ArrayBuffer
import io.github.edouardfouche.utils.StopWatch
import objects.estimators.MCDE
import objects.bound.{Chernoff, Hoeffding}
import objects.utility.{Identity, None}
import utils.helper.{default_snapshot, update_solution, wait_nonblocking}
import utils.types.{Snapshot, Solution}
import utils.{ComputationOverheadTracker, LESExecutionTimeTracker, PerformanceObserver, _Snapshot}

import scala.math.{max, sqrt}


trait Strategy extends Repeatable {
    var x: Double = 1.0  // TODO: What is x?
    var estimators = Array[IterativeDependencyEstimator]()
    val bound: Bound = new Hoeffding()
    val utility_function: Utility = new Identity()
    var m: Int = 10
    protected var epsilon = 0.01
    protected var sleep = 0.0 // [ms]
    protected val burnInPhaseLength = 3
    protected var experimentId = 0

    def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = {
        val dM = -bound.dM(solution, eps = epsilon)
        val ddM = -bound.ddM(solution, eps = epsilon)
//        println("***")
//        println("Using bound to compute m")
//        println("ratio gradient  = %s".format(dM/ddM))
//        println("ratio time  = %s".format(t_cs/t_1))
        val m = get_m(dM, ddM, t_cs, t_1)
        m
    }

    def get_m(performanceObserver: PerformanceObserver, targetIndex: Int,
              t_cs: Double, t_1: Double): Int = {
        val dM = performanceObserver.get1stDerivationApproximation(targetIndex)
        val ddM = performanceObserver.get2ndDerivationApproximation(targetIndex)
        println("***")
        println("ratio gradient  = %s".format(dM/ddM))
        println("ratio time  = %s".format(t_cs/t_1))
        get_m(dM, ddM, t_cs, t_1)
    }

    def get_m(dM: Double, ddM: Double, t_cs: Double, t_1: Double): Int = {
        val A = dM
        val B = 0.5*ddM
        val C = t_cs
        val D = t_1
        // enter this in wolfram alpha:
        // ((a+2*x*b)(c+x*d)-(x*a+x^2*b)*d)/((c+x*d)^2) = 0
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

    def select_active_targets(until: Double, targets: ArrayBuffer[(Int, Int)], results: Array[Array[_Snapshot]]): Array[Int] = {
        targets.zipWithIndex.filter(item => {
            val quality = results(item._1._1)(item._1._2).getQuality()
            quality < until
        }).map(_._2).toArray
    }

    def run(data: Array[Array[Double]], until: Double, currentRepetition: Int): Array[_Snapshot] = { // Returns an array of matrices
        experimentId = currentRepetition
        val targets = ArrayBuffer[(Int, Int)]()
        val results = ArrayBuffer[Array[Array[_Snapshot]]]()

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

        var totalIterations = 0
        var r = 0
        val T_start = StopWatch.stop()._1
        // var totalRoundOverhead = 0.0
        var round_timestamp = StopWatch.stop()._1
        val timer = new ComputationOverheadTracker(targets.length)
        val execTimeCalculator = new LESExecutionTimeTracker(targets.length)
        var active_targets = targets.indices.toArray
        execTimeCalculator.startTimer()
        timer.startTimer()
        while (active_targets.nonEmpty) {
            val iterating_start = StopWatch.stop()._1
            val round_results: Array[Array[_Snapshot]] = if (r == 0) {
                Array.ofDim(num_elements, num_elements)
            } else {
                results.last.map(_.clone())
            }
            for (i <- active_targets) {
                val p = targets(i)
                val current_result = if (r == 0) { null } else { round_results(p._1)(p._2).getSolution() }
                val (tSwitch, t1) = timer.getTimeModelForTarget(i)
                val (tsCalc, t1Calc) = execTimeCalculator.getTimeModel(i)
                val iterations = if (burnInPhaseFinished(r)) { get_m(current_result, tSwitch, t1) } else { m * (r+1) }
                val (dependency_update, time, variance) = estimators(i).run(pdata, Set(p._1, p._2), iterations)
                val result = (dependency_update, iterations, variance)
                val updated_result = update_solution(current_result, result)
                totalIterations = totalIterations + iterations
                val T = StopWatch.stop()._1 - T_start
                val quality = 1 - bound.value(updated_result, epsilon)
                val utility = utility_function.compute(updated_result)
                wait_nonblocking(sleep)
                val newSnapshot = _Snapshot(
                    result = updated_result._1,
                    quality = quality,
                    utility = utility,
                    tsMeasured = tSwitch,
                    t1Measured = t1,
                    tsCalculated = tsCalc,
                    t1Calculated = t1Calc,
                    variance = updated_result._3,
                    totalIterations = totalIterations,
                    m = iterations,
                    M = updated_result._2.toDouble,
                    round = r,
                    target = i,
                    T = T,
                    experimentRepetition = currentRepetition
                )
                round_results(p._1)(p._2) = newSnapshot
                round_results(p._2)(p._1) = newSnapshot
                timer.updateTimeModelParameters(target = i, algDuration = time, m = iterations)
                execTimeCalculator.updateTimeModel(i, iterations)
            }
            val iterating_duration = StopWatch.stop()._1 - iterating_start
            results.append(round_results.clone())
            val round_processing_time = StopWatch.stop()._1 - round_timestamp - iterating_duration
            round_timestamp = StopWatch.stop()._1
            //timer.trackRoundOverheadPerTarget(round_processing_time / active_targets.length)
            r = r + 1
            //totalRoundOverhead += timer.getTotalRoundOverhead(active_targets.length)
            if (burnInPhaseFinished(r)) {
                active_targets = select_active_targets(until,
                    targets = targets,
                    results = results.last)
            }
        }
        printStatistics(StopWatch.stop()._1 - T_start)
        getUpperTriangles(results)
    }

    protected def getUpperTriangles(results: ArrayBuffer[Array[Array[_Snapshot]]]): Array[_Snapshot] = {
        results.map(_.zipWithIndex.flatMap { case (r,i) => r.drop(i+1) }).toArray.flatten
    }

    protected def burnInPhaseFinished(roundIndex: Int): Boolean = roundIndex >= burnInPhaseLength

    protected def printStatistics(totalRuntime: Double): Unit = {
        println("Total runtime of %s = %s".format(name, totalRuntime))
        // println("Total round overhead of %s = %s".format(name, totalRoundOverhead))
    }
}