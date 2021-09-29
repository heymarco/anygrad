package traits

import scala.collection.mutable.ArrayBuffer
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import utils.types.Snapshot
import utils.{CSV, FileUtils, _Snapshot}
import utils.helper.variance

trait Repeatable {
    def name: String

    var max_result_quality = 0.99

    def run(data: Array[Array[Double]], until: Double, currentRepetition: Int): Array[_Snapshot]

    def repeat_strategy(data: Array[Array[Double]], N: Int, write: Boolean = true, target_dir: String): Array[_Snapshot] = {
        var result_buffer: Array[_Snapshot] = Array.empty
        val _ = run(data, until = max_result_quality, -1)  // cold start run
        for (i <- 0 until N) {
            val result = run(data, until = max_result_quality, currentRepetition = i)
            result_buffer = result_buffer ++ result
        }
        if (write) {
            val header = result_buffer(0).getHeader()
            val data = result_buffer.map { item =>
                item.getRow();
            }
            CSV.write(header, data, filename = name + ".csv")
        }
        result_buffer
    }
}
