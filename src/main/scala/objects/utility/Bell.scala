package objects.utility

import scala.math._
import traits.Utility
import utils.types._

class Bell extends Utility {

	override def compute(s: Solution, w: Double = 1): Double = {
		val k = 4
		exp(-k*pow(w, 2))
	}
}