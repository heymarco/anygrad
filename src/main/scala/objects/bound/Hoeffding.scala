package objects.bound

import scala.math._
import traits.Bound
import utils.helper.bounded
import utils.types._

class Hoeffding extends Bound {

	def value(input: Solution, eps: Double): Double = {
		exp(-2 * input._2 * eps * eps)
	}
	
	def dM(input: Solution, eps: Double): Double = {
		-2 * eps * eps * exp(-2 * input._2 * eps * eps)
	}

	def ddM(input: Solution, eps: Double): Double = {
		-2 * eps * eps * dM(input, eps)
	}

	def dEps(input: Solution, eps: Double): Double = {
		-8 * eps * input._2 * exp(-2 * input._2 * eps * eps)
	}

	def M(input: Solution, eps: Double, gamma: Double): Int = {
		(- (log((1 - gamma) / 2)) / (2 * eps * eps)).toInt
	}

	//TODO: implement confidence interval
	override def confidence(input: (Double, Int, (Int, Double, Double)), delta: Double): Double = ???
}

