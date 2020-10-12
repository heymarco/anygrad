package objects.bound

import scala.math._
import traits.Bound
import utils.helper._
import utils.types._

class Chernoff extends Bound {

	def value(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		2 * exp(-m * eps * eps / (2 * _var))
	}

	def dM(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		-eps * eps / _var * exp(-m * eps * eps / (2 * _var))
	}

	def ddM(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		-eps * eps / (2 * _var) * dM(input, eps)
	}

	def dEps(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		-2.0 * eps * m / _var * exp(-m * eps * eps / (2 * _var))
	}

	def M(input: Solution, eps: Double, gamma: Double): Int = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		(2 * _var / (eps * eps) * log(2 / (1 - gamma))).toInt
	}

	override def confidence(input: Solution, delta: Double = 0.95): Double = {
		val v = variance(input._3)
		math.sqrt(2*v*(math.log(2)-math.log(delta))/input._2)
	}
}