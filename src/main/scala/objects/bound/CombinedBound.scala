package objects.bound

import scala.math._
import traits.Bound
import utils.helper._
import utils.types._

class CombinedBound extends Bound {

	private val hoeffding = new Hoeffding()
	private val chebyshev = new Chebyshev()
	private val chernov = new Chernov()

	def value(input: Solution, eps: Double): Double = {
		val (v, m, _var) = input
		val _c_hoeffding = hoeffding.value(input, eps)
		if (m < 30) {
			return _c_hoeffding
		}
		val _c_chebyshev = chebyshev.value(input, eps)
		if (variance(_var) < 0.25) {
			val _c_chernov = chernov.value(input, eps)
			if (_c_chernov < _c_chebyshev) { return _c_chernov } else { return _c_chebyshev }
		}
		else {
			val _c_hoeffding = hoeffding.value(input, eps)
			if (_c_hoeffding < _c_chebyshev) { return _c_hoeffding } else { return _c_chebyshev }
		}
	}

	def dM(input: Solution, eps: Double): Double = {
		val (v, m, _var) = input
		if (m < 30) {
			return hoeffding.dM(input, eps)
		}
		val _c_hoeffding = hoeffding.value(input, eps)
		val _c_chebyshev = chebyshev.value(input, eps)
		if (variance(_var) < 0.25) {
			val _c_chernov = chernov.value(input, eps)
			if (_c_chernov < _c_chebyshev) { return chernov.dM(input, eps) } else { return chebyshev.dM(input, eps) }
		}
		else {
			val _c_hoeffding = hoeffding.value(input, eps)
			if (_c_hoeffding < _c_chebyshev) { return hoeffding.dM(input, eps) } else { return chebyshev.dM(input, eps) }
		}
	}

	def ddM(input: Solution, eps: Double): Double = {
		val (v, m, _var) = input
		if (m < 30) {
			return hoeffding.ddM(input, eps)
		}
		val _c_hoeffding = hoeffding.value(input, eps)
		val _c_chebyshev = chebyshev.value(input, eps)
		if (variance(_var) < 0.25) {
			val _c_chernov = chernov.value(input, eps)
			if (_c_chernov < _c_chebyshev) { return chernov.ddM(input, eps) } else { return chebyshev.ddM(input, eps) }
		}
		else {
			val _c_hoeffding = hoeffding.value(input, eps)
			if (_c_hoeffding < _c_chebyshev) { return hoeffding.ddM(input, eps) } else { return chebyshev.ddM(input, eps) }
		}
	}

	def dEps(input: Solution, eps: Double): Double = {
		val (v, m, _var) = input
		if (m < 30) {
			return hoeffding.dEps(input, eps)
		}
		val _c_hoeffding = hoeffding.value(input, eps)
		val _c_chebyshev = chebyshev.value(input, eps)
		if (variance(_var) < 0.25) {
			val _c_chernov = chernov.value(input, eps)
			if (_c_chernov < _c_chebyshev) { return chernov.dEps(input, eps) } else { return chebyshev.dEps(input, eps) }
		}
		else {
			val _c_hoeffding = hoeffding.value(input, eps)
			if (_c_hoeffding < _c_chebyshev) { return hoeffding.dEps(input, eps) } else { return chebyshev.dEps(input, eps) }
		}
	}

	def M(input: Solution, eps: Double, gamma: Double): Int = {
		val (v, m, _var) = input
		if (m < 30) {
			return hoeffding.M(input, eps, gamma)
		}
		val _c_chebyshev = chebyshev.value(input, eps)
		if (variance(_var) < 0.25) {
			val _c_chernov = chernov.value(input, eps)
			if (_c_chernov < _c_chebyshev) { return chernov.M(input, eps, gamma) } else { return chebyshev.M(input, eps, gamma) }
		}
		else {
			val _c_hoeffding = hoeffding.value(input, eps)
			if (_c_hoeffding < _c_chebyshev) { return hoeffding.M(input, eps, gamma) } else { return chebyshev.M(input, eps, gamma) }
		}
	}
}

