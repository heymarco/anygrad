/**
  * Created by Marco Heyden on 05.06.2019.
*/

package objects.utility

import scala.math._
import traits.Utility
import utils.types._

class ExponentialWeight extends Utility {

	override def compute(s: Solution, w: Double = 0.999): Double = {
		exp(-3.0 * s._1 * (1.0 - w))
	}
}