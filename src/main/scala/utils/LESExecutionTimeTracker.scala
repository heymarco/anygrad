package utils

import scala.collection.mutable.Map
import io.github.edouardfouche.utils.StopWatch
import utils.helper.{median_double}

import scala.collection.mutable.Queue

class LESExecutionTimeTracker(numTargets: Int, windowSize: Int = 1) {

    private val timeModelQueues: Array[Queue[(Double, Double)]] = Array.fill(numTargets)(Queue.empty)
    private val iterationsTimes: Map[Int, (Int, Double)] = Map.empty

    private var previousTimestamp: Double = 0

    def startTimer(): Unit = {
        previousTimestamp = StopWatch.stop()._1
    }

    def getTimeModel(forIndex: Int): (Double, Double) = {
        val thisQueue = timeModelQueues(forIndex)
        if (thisQueue.length == 0) {
            return (Double.NaN, Double.NaN)
        }
        val ts = median_double(thisQueue.map(_._1).toArray)
        val t1 = median_double(thisQueue.map(_._2).toArray)
        (ts, t1)
    }

    def updateTimeModel(index: Int, newIter: Int): Unit = {
        val newTimestamp = StopWatch.stop()._1
        val duration = newTimestamp - previousTimestamp
        if (iterationsTimes.contains(index)) {
            val previousMeasurement = iterationsTimes(index)
            val iter0 = previousMeasurement._1
            val duration0 = previousMeasurement._2
            if (iter0 != newIter) {
                val (ts, t1) = calculateFromMetrics(iter0, duration0, newIter, duration)
                timeModelQueues(index).enqueue((ts, t1))
                while (timeModelQueues(index).length > windowSize) {
                    timeModelQueues(index).dequeue()
                }
            }
        }
        iterationsTimes.update(index, (newIter, duration))
        previousTimestamp = newTimestamp
    }

    private def calculateFromMetrics(iter0: Int, duration0: Double, iter1: Int, duration1: Double): (Double, Double) = {
        val t1 = (duration1-duration0)/(iter1-iter0)
        val ts = duration0 - iter0*t1
        (ts, t1)
    }


}
