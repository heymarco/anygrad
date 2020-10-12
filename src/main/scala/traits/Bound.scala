/**
  * Created by Marco Heyden on 04.06.2019.
*/

package traits

import utils.types._

trait Bound {
	def value(input: Solution, epsilon: Double): Double
	def dM(input: Solution, epsilon: Double): Double
	def ddM(input: Solution, epsilon: Double): Double
	def dEps(input: Solution, epsilon: Double): Double
	def M(input: Solution, epsilon: Double, gamma: Double): Int
	def confidence(input: Solution, delta: Double = 0.95): Double
}