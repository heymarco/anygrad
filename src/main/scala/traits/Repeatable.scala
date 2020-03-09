package traits

import scala.collection.mutable.ArrayBuffer
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

import utils.types.Snapshot
import utils.FileUtils

trait Repeatable {
    def name: String

    var max_result_quality = 0.9

    def run(data: Array[Array[Double]], until: Double): Array[Array[Array[Snapshot]]]

    def repeat_strategy(data: Array[Array[Double]], N: Int, write: Boolean = true): Array[Array[Array[Array[Snapshot]]]] = {
        var result_buffer = ArrayBuffer[Array[Array[Array[Snapshot]]]]()
        for (i <- 0 until N) {
            val result = run(data, until = max_result_quality)
            result_buffer.append(result)
        }
        val result_arr = result_buffer.toArray
        print("Printing")
        if (write) {
            val json = convert_to_json(result_arr)
            FileUtils.saveOject(json, to_dir = "./save/" + name + ".txt")
        }
        result_arr
    }

    def convert_to_json(result: Array[Array[Array[Array[Snapshot]]]]): String = { // run, round, matrix
        val result_array = ArrayBuffer[Array[Array[Array[Map[String, Double]]]]]()
        for (run <- result) {
            val result_run = ArrayBuffer[Array[Array[Map[String, Double]]]]() // array of matrices
            for (round <- run) {
                val result_round = ArrayBuffer[Array[Map[String, Double]]]() // matrix of round
                for (row <- round) {
                    val result_row = ArrayBuffer[Map[String, Double]]() // matrix of round
                    for (item <- row) {
                        val (solution, quality, utility, iterations, time) = item
                        result_row.append(Map[String, Double](
                            "D" -> solution._1,
                            "Q" -> quality,
                            "U" -> utility,
                            "m" -> solution._2,
                            "M" -> iterations,
                            "T" -> time
                        ))
                    }
                    result_round.append(result_row.toArray)
                }
                result_run.append(result_round.toArray)
            }
            result_array.append(result_run.toArray)
        }
        implicit val formats = Serialization.formats(NoTypeHints)
        write(result_array.toArray)
    }
}
