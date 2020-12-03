package utils

import Numeric.Implicits._
import scala.collection.mutable.Queue


class PerformanceObserver(numTargets: Int) {
    private val queue: Array[Queue[(Int, Double)]] = Array.fill(numTargets)(Queue.empty)
    private val regressor = new LinearRegressor

    def enqueue(newValue: (Int, Double), at: Int, maxSize: Int = 10) = {
        queue(at).enqueue(newValue)
        while (queue(at).length > maxSize) {
            queue(at).dequeue
        }
    }

    def get1stDerivationApproximation(forTarget: Int): Double = {
        val thisQueue = queue(forTarget)
        var y = getQuality(thisQueue)
        y = y.slice(1, y.length)  // prune to same length as 2nd derivation estimation
        val x = getX(thisQueue)
        regressor.fit(x, y)
        regressor.getSlope
    }

    def get2ndDerivationApproximation(forTarget: Int): Double = {
        val thisQueue = queue(forTarget)
        val diffY = getDiff(getQuality(thisQueue))
        val diffX = getDiff(getIterations(thisQueue)).map(_.toDouble)
        val y = diffY.zipWithIndex.map { case (value, index) => value / diffX(index) }
        regressor.fit(getX(thisQueue), y)
        print(getX(thisQueue).size, y.size)
        regressor.getSlope
    }

    private def getDiff[T: Numeric](seq: Seq[T]): Seq[T] = {
        seq.sliding(2).map { case window => window(1) - window(0) }.toSeq
    }

    private def getQuality(queue: Queue[(Int, Double)]): List[Double] = queue.map(_._2).toList

    private def getIterations(queue: Queue[(Int, Double)]): List[Int] = queue.map(_._1).toList

    private def getX(queue: Queue[(Int, Double)]): List[Double] = {
        getIterations(queue).slice(1, queue.length).map(_.toDouble)
    }

    private def getMeanGradient(inputX: Seq[Double], inputY: Seq[Double]): Double = {
        val gradients = inputY.zipWithIndex.map { case (value, index) => value / (inputX(index)) }
        var diffGradients = getDiff(gradients)
        diffGradients = diffGradients.zipWithIndex.map { case (value, index) =>
            2 * value / (inputX(index+1) + inputX(index))
        }
        diffGradients.sum / diffGradients.length
    }
}
