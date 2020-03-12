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


object Main extends LazyLogging {
    def main(args: Array[String]): Unit = {
        val unit = "ms"


        info("Working directory: " + System.getProperty("user.dir"))
        info("Raw parameters given: " + args.map(s => "\"" + s + "\"").mkString("[", ", ", "]"))

        val MCDE_Stats = Vector("mwp")

        require(args.length > 0, "No arguments given. Please see README.md")

        var args_map: Map[String, String] = Map()
        for (i <- 0 until args.length - 1) {
            if ( i % 2 == 0) { args_map = args_map + (args(i) -> args(i + 1)) }
        }
        val experiment = if (args_map("-e") == "t-over-m") {
            new ToverM()
        }
        else if (args_map("-e") == "anygrad") {
            new ToverM()
        }
        else {
            new ToverM()
        }
        StopWatch.start
        experiment.run(args_map)

        System.exit(0)
    }

    def info(s: String): Unit = logger.info(s)
    def warn(s: String): Unit = logger.warn(s)
}
