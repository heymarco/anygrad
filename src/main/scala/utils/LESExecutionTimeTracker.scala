package utils

import io.github.edouardfouche.utils.StopWatch
import shapeless.ops.tuple.Length
import spire.syntax.LiteralDoubleEuclideanRingOps

import scala.collection.mutable.Queue

class LESExecutionTimeTracker(numTargets: Int, windowSize: Int = 30) {

    private val timeModelQueues: Array[Queue[(Double, Double)]] = Array.fill(numTargets)(Queue.empty)
    private val iterationsTimes: Array[(Int, Double)] = Array.empty

    private var previousTimestamp: Double = 0

    def startTimer(): Unit = {
        previousTimestamp = StopWatch.stop()._1
    }

    def getTimeModel(forIndex: Int): (Double, Double) = {
        val thisQueue = timeModelQueues(forIndex)
        val ts = thisQueue.map(_._1).sum / thisQueue.length
        val t1 = thisQueue.map(_._2).sum / thisQueue.length
        (ts, t1)
    }

    def updateTimeModel(index: Int, newIter: Int): Unit = {
        val newTimestamp = StopWatch.stop()._1
        val duration = newTimestamp - previousTimestamp
        val previousMeasurement = iterationsTimes(index)
        val iter0 = previousMeasurement._1
        val duration0 = previousMeasurement._2
        val result = calculateFromMetrics(iter0, duration0, newIter, duration)
        timeModelQueues(index).enqueue(result)
        while (timeModelQueues(index).length > windowSize) {
            timeModelQueues(index).dequeue()
        }
        iterationsTimes(index) = (newIter, duration)
        previousTimestamp = newTimestamp
    }

    private def calculateFromMetrics(iter0: Int, duration0: Double, iter1: Int, duration1: Double): (Double, Double) = {
        val t1 = (duration1-duration0)/(iter1-iter0)
        val ts = duration0 - iter0*t1
        (ts, t1)
    }


}
