package utils

import scala.collection.mutable.Queue
import io.github.edouardfouche.utils.StopWatch

class MeasuresSwitchingTime {
	private var window_size = 30
	private var start_time = 0.0					// the time the round started
	private var end_time = 0.0		
	private var round_overhead_per_target = 0.0		// the time the round ended

	val queue_tcs = new Queue[Double]
	val queue_t1 = new Queue[Double]

	def track_start_time() {
		start_time = StopWatch.stop()._1
	}

	def track_end_time() {
		end_time = StopWatch.stop()._1
	}

	private def enqueue(q: Queue[Double], new_val: Double, max_size: Int): Unit = {
		q.enqueue(new_val)
		while (q.size > max_size) {
			q.dequeue
		}
	}

	def calculate_switching_time(aa_duration: Double, m: Int): (Double, Double) = {
		val total = end_time - start_time
		val t_switch = total - aa_duration
		val t_1 = aa_duration / m
		enqueue(queue_tcs, t_switch, window_size)
		enqueue(queue_t1, t_1, window_size)
		val t_switch_ma = queue_tcs.sum / queue_tcs.size + round_overhead_per_target
		val t1_ma = queue_t1.sum / queue_t1.size
		(t_switch_ma, t1_ma)
	}

	def track_round_processing_overhead(duration: Double, num_targets: Int) = {
		round_overhead_per_target = duration / num_targets
	}
}
