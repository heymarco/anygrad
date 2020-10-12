/**
  * Created by Marco Heyden on 05.06.2019.
*/

package objects.utility

import traits.Utility
import utils.types._

class Identity extends Utility {
    override def compute(s: (Double, Int, (Int, Double, Double)), w: Double): Double = { w * s._1 }
}