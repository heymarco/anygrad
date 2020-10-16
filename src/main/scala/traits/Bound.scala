/**
  * Created by Marco Heyden on 04.06.2019.
*/

package traits

import utils.types._

trait Bound {
	def value(input: Solution, eps: Double): Double
	def dM(input: Solution, eps: Double): Double
	def ddM(input: Solution, eps: Double): Double
	def dEps(input: Solution, eps: Double): Double
	def M(input: Solution, eps: Double, gamma: Double): Int
	def confidence(input: Solution, delta: Double = 0.01): Double
}