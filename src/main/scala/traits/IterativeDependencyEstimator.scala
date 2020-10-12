/**
  * Created by Marco Heyden on 28.04.2019.
*/

package traits

import io.github.edouardfouche.index.Index
import io.github.edouardfouche.mcde.Stats
import io.github.edouardfouche.mcde.StatsFactory

import utils.types._


trait IterativeDependencyEstimator {

	val approach: Stats

	def preprocess(data: Array[Array[Double]]): Index

	// Runs the anytime algorithm for a certain number of iterations
	// data: the preprocessed and indexed data
	// p: the relevant target
	// m: the number of iterations to add to the target
	// returns: (contrast, delta t, variance)
	def run(data: Index, p: Set[Int], m: Int): (Double, Double, (Int, Double, Double)) = {
		approach.setM(m)
		approach.contrast_and_time(data.asInstanceOf[approach.PreprocessedData], p)
	}
}