package traits

import scala.collection.parallel.CollectionConverters._
import io.github.edouardfouche.preprocess.Preprocess

import traits.Repeatable

trait Experiment {
    var N: Int = 1
    var strategies = Array[Repeatable]()
    var parallel = true
    var target_dir = "./save"
    var file_dir = ""

    def init_strategies()

    def setup(args: Map[String, String]): Unit = {
        parallel = args.getOrElse("-p", "1").toInt == 1
        N = args.getOrElse("-r", "1").toInt
        target_dir = args("-t")
        file_dir = args("-f")
    }

    def load_data(path_to_file: String): Array[Array[Double]] = {
        if (!new java.io.File(path_to_file).exists) {
            println(s"Path to file: ${path_to_file}")
            throw new Error(s"Path to file is unvalid")
        }
        Preprocess.open(path_to_file, header = 1, separator = ",", excludeIndex = true, dropClass = true)
    }

    def run(args: Map[String, String]) = {
        setup(args)
        init_strategies()
        // load data
        val data = load_data(file_dir).transpose
        // run strategies
        if (parallel) {
            println("Running parallel")
            strategies.par.foreach(strategy => {
                val result = strategy.repeat_strategy(data, N, write=true, target_dir=target_dir) // data, n, write
            })
        }
        else {
            println("Running sequential")
            strategies.foreach(strategy => {
                val result = strategy.repeat_strategy(data, N, write=true, target_dir=target_dir) // data, n, write
            })
        }
    }
}
