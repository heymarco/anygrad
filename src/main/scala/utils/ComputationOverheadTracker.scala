package utils

import scala.collection.mutable.Queue
import io.github.edouardfouche.utils.StopWatch

class ComputationOverheadTracker(numTargets: Int) {
	private var start_time = 0.0		// the time the round started
	private var end_time = 0.0			// the time the round ended

	private val roundOverheadPerTarget: Queue[Double] = Queue.empty
	private val timeModelQueues: Array[Queue[(Double, Double)]] = Array.fill(numTargets)(Queue.empty)

	private def enqueueSwitchingTime(newValue: (Double, Double), at: Int, maxSize: Int = 5) = {
		timeModelQueues(at).enqueue(newValue)
		while (timeModelQueues(at).length > maxSize) {
			timeModelQueues(at).dequeue
		}
	}

	def trackRoundOverheadPerTarget(overheadPerTarget: Double, queueMaxSize: Int = 30): Unit = {
		roundOverheadPerTarget.enqueue(overheadPerTarget)
		while (roundOverheadPerTarget.length > queueMaxSize) {
			roundOverheadPerTarget.dequeue
		}
	}

	def updateTimeModelParameters(target: Int, algDuration: Double, m: Int): Unit = {
		val total = end_time - start_time
		val tSwitch = total - algDuration
		val t1 = algDuration / m
		enqueueSwitchingTime((tSwitch, t1), at = target)
	}

	def getTimeModelForTarget(at: Int): (Double, Double) = {
		val tSwitch = timeModelQueues(at).map(_._1)
		val t1 = timeModelQueues(at).map(_._2)
		(
			tSwitch.sum / tSwitch.length + getRoundOverheadPerTarget(),
			t1.sum / t1.length
		)
	}

	def setSignalStart(): Unit = {
		start_time = StopWatch.stop()._1
	}

	def setSignalEnd(): Unit = {
		end_time = StopWatch.stop()._1
	}

	def getRoundOverheadPerTarget(): Double = {
		roundOverheadPerTarget.sum / roundOverheadPerTarget.length
	}

	def getTotalRoundOverhead(numActiveTargets: Int): Double = {
		getRoundOverheadPerTarget() * numActiveTargets
	}
}
