package traits

import io.github.edouardfouche.utils.StopWatch

trait MeasuresSwitchingTime {
	// Accumulated time spent on iterations in round 1
	// !!! Only works if running sequential
	private var t_m_total = 0.0
	private var start_time = 0.0
	private var end_time = 0.0
	private var t_cs_prev = 0.0
	private var t_1_prev = 0.0
	private var counter = 0

	def init_measuring_switching_cost() {
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
		val duration = end_time - start_time
		val m = m_round.toDouble / N
		val t_cs_new = ( duration - t_m_total ) / N
		val t_1_new = ( duration / N - t_cs_new ) / m
		val t_cs = (counter * t_cs_prev + t_cs_new) / (counter+1)
		val t_1 = (counter * t_1_prev + t_1_new) / (counter+1)
		counter += counter
		val measurement = (t_cs, t_1)
		println(measurement)
		measurement
	}
}
