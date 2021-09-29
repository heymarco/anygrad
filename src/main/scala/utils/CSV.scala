package utils

import com.github.tototoshi.csv._
import java.io._

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.Seq

import utils.types._
import utils.helper._

object CSV {
	def write(header: Seq[String], data: Seq[Seq[Any]], filename: String): Unit = {
		val dataStrings = data.map(row => row.map(_.toString))
		val allRows: Seq[Seq[String]] = header +: dataStrings
		val csv: String = allRows.map(_.mkString(",")).mkString("\n")
		val filepath = "save/experiments/" + filename
		new PrintWriter(filepath) { write(csv); close() }
	}
}





