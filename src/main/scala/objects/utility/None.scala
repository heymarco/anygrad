/**
  * Created by Marco Heyden on 05.06.2019.
*/

package objects.utility

import scala.math._
import traits.Utility
import utils.types._

class None extends Utility {
	override def compute(s: Solution, w: Double = 0.999): Double = { 1.0 }
}