package objects.utility

import scala.math._
import traits.Utility
import utils.types._

class AbsoluteValueLinear extends Utility {
	override def compute(s: Solution, w: Double = 1.0): Double = { 
		2 * abs(s._1 - 0.5)
	}
}