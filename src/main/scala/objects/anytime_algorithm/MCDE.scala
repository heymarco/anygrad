/**
  * Created by Marco Heyden on 28.04.2019.
*/

package objects.estimators

import io.github.edouardfouche.index.Index
import io.github.edouardfouche.mcde.Stats
import io.github.edouardfouche.mcde.StatsFactory

import objects.bound.Hoeffding
import traits.Bound
import traits.IterativeDependencyEstimator
import utils.types._

class MCDE extends IterativeDependencyEstimator {

	val test: String = "MWP"
	val parallelize = 0

	def approach(): Stats = {
		StatsFactory.getTest(test, m, 0.5, 0.5, false, parallelize)
	}

	def preprocess(data: Array[Array[Double]]): PreprocessedData = {
		approach().preprocess(data.transpose)
	}
}

