package io.github.edouardfouche.mcde

import io.github.edouardfouche.index.Index
import io.github.edouardfouche.utils.StopWatch

/**
  * Created by fouchee on 07.07.17.
  */
trait Stats {
  type PreprocessedData <: Index // PreprocessedData are subtypes of Index, which are column oriented structures
  val id: String
  val alpha: Double
  val beta: Double
  val M: Int
  protected var M_variable: Int = 30

  def setM(M: Int): Unit = {
    M_variable = M
  }

  /**
    * @param input A data set (row oriented)
   */
  def preprocess(input: Array[Array[Double]]): PreprocessedData

  /**
    * @param m A data set (row oriented)
    */
  def contrast(m: Array[Array[Double]], dimensions: Set[Int]): Double = {
    this.contrast(this.preprocess(m), dimensions)
  }

  def contrast(m: PreprocessedData, dimensions: Set[Int]): Double = {
    contrast_v(m, dimensions)._1
  }

  def contrast_v(m: PreprocessedData, dimensions: Set[Int]): (Double, (Int, Double, Double))

  def contrast_and_time(m: PreprocessedData, dimensions: Set[Int]): (Double, Double, (Int, Double, Double)) = {
    val prev_t = StopWatch.stop()._1
    val (c, variance) = contrast_v(m, dimensions)
    val after_t = StopWatch.stop()._1
    (c, after_t - prev_t, variance)
  }

  /**
    * @param m A data set (row oriented)
    */
  def contrastMatrix(m: Array[Array[Double]]): Array[Array[Double]] = {
    this.contrastMatrix(this.preprocess(m))
  }

  def contrastMatrix(m: PreprocessedData): Array[Array[Double]]
}
