package traits

import io.github.edouardfouche.utils.StopWatch

trait MeasuresSwitchingTime {
	// Initial number of iterations
	def get_m_initial(): Int
	// Estimated switching time
	var t_cs = 1.0
	// Estimated time for one iteration
	var t_1 = 1.0
	// Accumulated time spent on iterations in round 1
	private var t_m_total = 0.0
	private var start_time = 0.0
	private var end_time = 0.0

	def init_measuring_switching_cost() {
		t_m_total = 0.0
	}

	def track_computation_time(t: Double) {
		t_m_total += t
	}

	def track_start_time() {
		start_time = StopWatch.stop()._1
	}

	def calculate_switching_time(N: Int) {
		end_time = StopWatch.stop()._1
		val duration = end_time - start_time
		t_cs = ( duration - t_m_total ) / N
		t_1 = ( duration / N - t_cs ) / get_m_initial()
	}
}