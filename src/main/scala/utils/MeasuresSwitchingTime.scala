package utils

import scala.collection.mutable.Queue
import io.github.edouardfouche.utils.StopWatch

class MeasuresSwitchingTime {
	// Accumulated time spent on iterations in round 1
	private var t_m_total = 0.0		// the total time spent iterating in this round
	private var start_time = 0.0	// the time the round started
	private var end_time = 0.0		// the time the round ended
	private var t_cs_prev = 0.0		// the switching time of the previous round
	private var t_1_prev = 0.0		// the iterating time of the previous round

	val queue_tcs = new Queue[Double]
	val queue_t1 = new Queue[Double]

	def enqueue(q: Queue[Double], new_val: Double, max_size: Int = 10): Unit = {
		q.enqueue(new_val)
		if (q.size > max_size) {
			q.dequeue
		}
	}

	def init_execution(): Unit = {
		t_m_total = 0.0
		t_cs_prev = 0.0
		t_1_prev = 0.0
		start_time = 0.0
		end_time = 0.0
	}

	def init_round(): Unit = {
		t_m_total = 0.0
	}

	def track_computation_time(t: Double) {
		t_m_total += t
	}

	def track_start_time() {
		start_time = StopWatch.stop()._1
	}

	def calculate_switching_time(N: Int, m_round: Int): (Double, Double) = {
		end_time = StopWatch.stop()._1
		val duration = end_time - start_time // contains switching time, wait time, processing
		val t_cs_new = ( duration - t_m_total ) / N
		val t_1_new = t_m_total / m_round
		enqueue(queue_tcs, t_cs_new)
		enqueue(queue_t1, t_1_new)
		val t_cs = queue_tcs.sum / queue_tcs.size
		val t_1 = queue_t1.sum / queue_t1.size
		val measurement = (t_cs, t_1)
		measurement
	}
}
