package utils

import com.github.tototoshi.csv._
import java.io._

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

import utils.types._
import utils.helper._

object CSV {

	var csv_dir = "../../results/experiment/default"
	var enabled: Boolean = false
	var total_iterations = 0

	private def data_dir(): String = {
		csv_dir + "/data"
	}

	private def deleteFiles(file: File): Unit = {
		if (file.isDirectory) {
			file.listFiles.foreach(deleteFiles)
		}
		if (file.exists && !file.isDirectory) {
			if (!file.delete) { throw new Exception(s"Unable to delete ${file.getAbsolutePath}") }
		}
	}

	def enable(e: Boolean) {
		enabled = e
		if (enabled) { emptyDir(data_dir()) }
	}

	private def emptyDir(dir: String) {
		val folder = new File(dir)
		deleteFiles(folder)
	}

	private def get_or_create(folder: String, name: String = "out.csv", dir: String = csv_dir): File = {
		new File(s"${dir}/${folder}/${name}")
	}

	def write_all(data: Array[Array[Double]], name: String = "repeated.csv", append: Boolean = false, names: List[String] = List()) {
		var data_t = data.transpose
		var list = ListBuffer[List[Double]]()
		for (i <- data_t.indices) {
			list += data_t(i).toList.asInstanceOf[List[Double]]
		}
		val f = get_or_create("data", name, csv_dir)
		val writer = CSVWriter.open(f, append=append)
		if (names.length > 0) {
			writer.writeRow(names)
		}
		writer.writeAll(list.toList)
		writer.close
	}

	def write_list(list: List[List[Double]], name: String, dir: String = ".") {
		val f = get_or_create(dir, name)
		val writer = CSVWriter.open(f, append=false)
		writer.writeAll(list)
		writer.close
	}

	def write_solutions(solutions: Array[Solution], dim: Int, name: String) {
		var arr = Array.ofDim[Double](dim, dim)
		var buffer = ListBuffer[List[Double]]()
		var k = 0
		for {
			i <- 0 until dim
			j <- 0 to i
		}
		yield {
			if (i == j) { 
				arr(i)(j) = 1.0
				arr(j)(i) = 1.0
			}
			else {
				arr(i)(j) = solutions(k)._1
				arr(j)(i) = solutions(k)._1
				k += 1
			}
		}
		write_all(arr, name)
	}

	def write_resource_distribution(solutions: Array[Solution], dim: Int, name: String) {
		var arr = Array.ofDim[Double](dim, dim)
		var buffer = ListBuffer[List[Double]]()
		var k = 0
		for {
			i <- 0 until dim
			j <- 0 to i
		}
		yield {
			if (i == j) { 
				arr(i)(j) = 1.0
				arr(j)(i) = 1.0
			}
			else {
				arr(i)(j) = solutions(k)._2
				arr(j)(i) = solutions(k)._2
				k += 1
			}
		}
		write_all(arr, name)
	}

	def write_oracle_indices(solutions: Array[Solution], name: String) {
		var target_index_arr = Array.ofDim[Int](solutions.length)

		var buffer = ListBuffer[(Int, Solution)]()
		for {
			i <- solutions.indices
		}
		yield {
			buffer += (i -> solutions(i))
		}
		var sorted_list = buffer.toList.sortBy(_._2._2) // sort by M
		var indices = Array.ofDim[Double](solutions.length)
		for {
			i <- sorted_list.indices
		}
		yield {
			var item = sorted_list(i)
			println(s"${item._2._2}")
			indices(i) = item._1.toDouble
		}
		val dbl_arr = Array(indices)
		write_all(dbl_arr, name)
	}
}





