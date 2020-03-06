/**
  * Created by Marco Heyden on 19.04.2019.
*/

package utils

import traits.Bound
import io.github.edouardfouche.mcde.Stats
import utils.helper._

object types {

	type Variance = (Int, Double, Double)

	type Solution = (Double, Int, Variance)

	type PreprocessedData = Stats#PreprocessedData
}