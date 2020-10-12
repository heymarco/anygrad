/**
  * Created by Marco Heyden on 05.06.2019.
*/

package traits

import utils.types._

trait Utility {
	var tau: Double = 1.0
	def compute(s: Solution, w: Double = 1.0): Double
}