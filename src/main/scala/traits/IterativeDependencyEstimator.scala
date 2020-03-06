/**
  * Created by Marco Heyden on 28.04.2019.
*/

package traits

import io.github.edouardfouche.index.Index
import io.github.edouardfouche.mcde.Stats
import io.github.edouardfouche.mcde.StatsFactory

import utils.types._


trait IterativeDependencyEstimator {

	type PreprocessedData = Stats#PreprocessedData

	var m: Int = 4

	def approach(): Stats

	def preprocess(data: Array[Array[Double]]): PreprocessedData

	// Runs the anytime algorithm for a certain number of iterations
	// data: the preprocessed and indexed data
	// p: the relevant target
	// m: the number of iterations to add to the target
	// returns: (contrast, delta t, variance)
	def run(data: PreprocessedData, p: Set[Int], m: Int): (Double, Double, (Int, Double, Double)) = {
		this.m = m
		val a = approach()
		a.contrast_and_time(data.asInstanceOf[a.PreprocessedData], p)
	}
}