package traits

import scala.collection.mutable.ArrayBuffer
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import utils.types.Snapshot
import utils.FileUtils
import utils.helper.variance

trait Repeatable {
    def name: String

    var max_result_quality = 0.95

    def run(data: Array[Array[Double]], until: Double): Array[Array[Array[Snapshot]]]

    def repeat_strategy(data: Array[Array[Double]], N: Int, write: Boolean = true, target_dir: String): Array[Array[Array[Array[Snapshot]]]] = {
        var result_buffer = ArrayBuffer[Array[Array[Array[Snapshot]]]]()
        for (i <- 0 until N) {
            val result = run(data, until = max_result_quality)
            result_buffer.append(result)
        }
        val result_arr = result_buffer.toArray
        if (write) {
            val json = convert_to_json(result_arr)
            FileUtils.saveOject(json, to_dir = target_dir + name + ".json")
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
                        val (solution, quality, utility, iterations, time, m, t_cs, t_1) = item
                        result_row.append(Map[String, Double](
                            "result" -> solution._1,
                            "variance" -> variance(solution._3),
                            "quality" -> quality,
                            "utility" -> utility,
                            "m" -> m,
                            "M" -> solution._2,
                            "iterations" -> iterations,
                            "duration" -> time,
                            "tcs" -> t_cs,
                            "t1" -> t_1
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
