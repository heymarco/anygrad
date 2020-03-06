/*
 * Copyright (C) 2018 Edouard Fouché
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import io.github.edouardfouche.preprocess.Preprocess
import io.github.edouardfouche.mcde.StatsFactory
import io.github.edouardfouche.utils.StopWatch
import com.typesafe.scalalogging.LazyLogging

import experiments._
import objects._strategy._
import utils.helper._
import utils.types._
import utils.CSV

/**
  * Created by fouchee on 01.06.17.
  */

// example usage: sbt "run HiCS src/test/resources/iris.csv"
// or sbt package and then
// scala target/scala-2.11/subspacesearch_2.11-1.0.jar GMD src/test/resources/iris.csv
// or sbt assembly and then
// java -jar target/scala-2.11/SubspaceSearch-assembly-1.0.jar GMD src/test/resources/iris.csv
// A real example: scala target/scala-2.11/subspacesearch_2.11-1.0.jar GMD /home/fouchee/git/SubspaceSearch/src/test/resources/11-12_25-26_37-38-39_55-56-57_40-41-42-43_46-47-48-49_30-31-32-33-34_73-74-75-76-77_data.txt

// Experiment1
// java -jar /home/fouchee/git/SubspaceSearch/target/scala-2.11/SubspaceSearch-assembly-1.0.jar com.edouardfouche.experiments.KS_MWB_extern
// scala /home/fouchee/git/SubspaceSearch/target/scala-2.11/subspacesearch_2.11-1.0.jar com.edouardfouche.experiments.KS_MWB

// this is a nice table: http://www.normaltable.com/
object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val unit = "ms"


    info("Working directory: " + System.getProperty("user.dir"))
    info("Raw parameters given: " + args.map(s => "\"" + s + "\"").mkString("[", ", ", "]"))

    val MCDE_Stats = Vector("mwp")

    require(args.length > 0, "No arguments given. Please see README.md")
    //StopWatch.start
    //require(args.length >= 4, "Arguments should consists in at least 2 items: The task '-t' to perform and the path '-f' to a file.")

    var args_map: Map[String, String] = Map()
    for (i <- 0 until args.length - 1) {
      if ( i % 2 == 0) { args_map = args_map + (args(i) -> args(i + 1)) }
    }

    val experiment = args("-e")


    val experiment = if (experiment == "t-over-m") {
      new ToverM()
    }
    experiment.run(args_map)

    System.exit(0)
  }

  def info(s: String): Unit = logger.info(s)
  def warn(s: String): Unit = logger.warn(s)
}
