package objects.bound

import scala.math._
import traits.Bound
import utils.helper._
import utils.types._

class Chebyshev extends Bound {

	def value(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		_var / (m * eps * eps)
	}

	def dM(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		-_var / (m * m * eps * eps)
	}

	def ddM(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		2 * _var / (pow(eps, 2) * pow(m, 3))
	}

	def dEps(input: Solution, eps: Double): Double = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		-2.0 * _var / (m * pow(eps, 3))
	}

	def M(input: Solution, eps: Double, gamma: Double): Int = {
		val (v, m, variance_data) = input
		val _var = variance(variance_data)
		(_var / ((1 - gamma) * eps * eps)).toInt
	}

	//TODO: implement confidence interval
	override def confidence(input: (Double, Int, (Int, Double, Double)), delta: Double): Double = ???
}