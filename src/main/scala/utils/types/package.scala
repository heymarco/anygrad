/**
  * Created by Marco Heyden on 19.04.2019.
*/

package utils

import scala.collection.mutable.Queue
import traits.Bound
import io.github.edouardfouche.mcde.Stats
import utils.helper._

object types {

	type Variance = (Int, Double, Double)

	type Solution = (Double, Int, Variance)

	type Snapshot = (Solution, Double, Double, Double, Double, Double, Double, Double)

	type PreprocessedData = Stats#PreprocessedData

}