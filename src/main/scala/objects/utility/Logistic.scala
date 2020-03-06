package objects.utility

import scala.math._
import traits.Utility
import utils.types._

class Logistic extends Utility {

	override def compute(s: Solution, w: Double = 1): Double = {
		val k = 15
		1 / (1 + exp(-k*(s._1 - tau)))
	}
}