/**
 * Created by Marco Heyden on 19.04.2019.
 */

package utils

import scala.language.postfixOps

import Array._
import java.io._
import scala.collection.mutable.ArrayBuffer
import util.Try

import utils.types._
import traits.Bound
import io.github.edouardfouche.utils.StopWatch

object helper {

    def getUpperTriangle(a: Array[Array[Any]]) =
        a.zipWithIndex.map{case(r,j) => r.drop(j)}

    def N(c: Double): Double = {
        return Math.abs(2 * c - 1)
    }

    def bounded(input: Double, min: Double, max: Double): Double = {
        var result: Double = input
        result = Math.min(result, max)
        result = Math.max(result, min)
        return result
    }

    def max_val(array: Array[Double]): Double = {
        var max = array(0);

        for (i <- 1 until array.length) {
            if (array(i) > max) max = array(i);
        }
        max
    }

    def mv(oldName: String, newName: String) = Try(new File(oldName).renameTo(new File(newName))).getOrElse(false)

    def imputed(matrix: Array[Array[Double]]): Array[Array[Double]] = {
        for (dataset <- matrix; i <- 0 until dataset.length) {
            var last_non_NaN: Double = 0
            if (dataset(i).isNaN) {
                dataset(i) = last_non_NaN
                println("NaN occured")
            }
            else last_non_NaN = dataset(i)
        }
        matrix
    }

    def update_solution(s_old: Solution, s_new: Solution): Solution = {

        if (s_old._2 == 0) {
            return s_new
        }
        val (v_old, m_old, variance_old) = s_old
        val (v_new, m_new, variance_new) = s_new
        val v = update_average(v_old, v_new, m_old, m_new)
        val m = m_old + m_new
        val variance = updateVarianceAggregates(variance_old, variance_new)
        var result = (v, m, variance)
        result
    }

    def update_average(avg_old: Double, new_val: Double, m_old: Int, m_new: Int): Double = {
        (avg_old * m_old + new_val * m_new) / (m_old + m_new)
    }

    def updateVariance(existingAggregate: (Int, Double, Double), newValue: Double): (Int, Double, Double) = {
        var (count, mean, m2) = existingAggregate
        count += 1
        val delta = newValue - mean
        mean += delta / count
        val delta2 = newValue - mean
        m2 += delta * delta2
        (count, mean, m2)
    }

    def updateVarianceAggregates(agg1: (Int, Double, Double), agg2: (Int, Double, Double)): (Int, Double, Double) = {
        val (count1, mean1, m2_1) = agg1
        val (count2, mean2, m2_2) = agg2
        val count = count1 + count2
        val mean = (mean1 * count1 + mean2 * count2) / count
        val delta = mean2 - mean1
        val M2 = m2_1 + m2_2 + delta * delta * count1 * count2 / count
        (count, mean, M2)
    }

    def variance(existingAggregate: (Int, Double, Double)): Double = {
        val (count, mean, m2) = existingAggregate
        if (count < 2) return Double.NaN
        m2 / (count - 1)
    }

    def matrix_to_list(array: Array[Array[Double]]): List[List[Double]] = {

        var list = List[List[Double]]()
        for (item <- array) {
            list = list ::: List(item.toList)
        }
        list
    }

    def string_to_array(string: String): Array[String] = {
        string.replace("[", "").replace("]", "").split(",")
    }

    def get_files(dir: String): List[String] = {
        val d = new File(dir)
        if (d.exists && d.isDirectory) {
            d.list.filter(_.endsWith(".csv")).toList
        } else {
            List[String]()
        }
    }

    def get_filename(path: String): String = {
        path.split("/").last
    }

    def median_int(arr: Array[Int]): Int = {

        val medians = arr grouped 5 map medianUpTo5 toArray;
        if (medians.size <= 5) medianUpTo5(medians)
        else median_int(medians)
    }

    private def medianUpTo5(five: Array[Int]): Int = {
        def order2(a: Array[Int], i: Int, j: Int) = {
            if (a(i) > a(j)) {
                val t = a(i); a(i) = a(j); a(j) = t
            }
        }

        def pairs(a: Array[Int], i: Int, j: Int, k: Int, l: Int) = {
            if (a(i) < a(k)) {
                order2(a, j, k); a(j)
            }
            else {
                order2(a, i, l); a(i)
            }
        }

        if (five.length < 2) return five(0)
        order2(five, 0, 1)
        if (five.length < 4) return (
            if (five.length == 2 || five(2) < five(0)) five(0)
            else if (five(2) > five(1)) five(1)
            else five(2)
            )
        order2(five, 2, 3)
        if (five.length < 5) pairs(five, 0, 1, 2, 3)
        else if (five(0) < five(2)) {
            order2(five, 1, 4); pairs(five, 1, 4, 2, 3)
        }
        else {
            order2(five, 3, 4); pairs(five, 0, 1, 3, 4)
        }
    }

    def median_double(arr: Array[Double]): Double = {

        val medians = arr grouped 5 map medianUpTo5 toArray;
        if (medians.size <= 5) medianUpTo5(medians)
        else median_double(medians)
    }

    private def medianUpTo5(five: Array[Double]): Double = {
        def order2(a: Array[Double], i: Int, j: Int) = {
            if (a(i) > a(j)) {
                val t = a(i); a(i) = a(j); a(j) = t
            }
        }

        def pairs(a: Array[Double], i: Int, j: Int, k: Int, l: Int) = {
            if (a(i) < a(k)) {
                order2(a, j, k); a(j)
            }
            else {
                order2(a, i, l); a(i)
            }
        }

        if (five.length < 2) return five(0)
        order2(five, 0, 1)
        if (five.length < 4) return (
            if (five.length == 2 || five(2) < five(0)) five(0)
            else if (five(2) > five(1)) five(1)
            else five(2)
            )
        order2(five, 2, 3)
        if (five.length < 5) pairs(five, 0, 1, 2, 3)
        else if (five(0) < five(2)) {
            order2(five, 1, 4); pairs(five, 1, 4, 2, 3)
        }
        else {
            order2(five, 3, 4); pairs(five, 0, 1, 3, 4)
        }
    }

    def wait_nonblocking(sleep: Double): Unit = {
        val start = StopWatch.stop()._1
        var now = start
        while (now - start < sleep) {
            now = StopWatch.stop()._1
        }
    }

    // (updated_result, quality, utility, M, T)
    def default_snapshot(default_solution: Solution, bound: Bound, eps: Double): Snapshot = {
        (default_solution, 1 - bound.value(default_solution, eps), 0.0, 0, 0.0)
    }
}





