package experiments

import strategies.{AnyGradSelectAll, Anygrad, Baseline}
import traits.{Experiment, Repeatable, Strategy}


class SingleRun extends Experiment {
    var m = 8
    var strategy_id = "baseline"

    override def setup(args: Map[String, String]): Unit = {
        super.setup(args)
        m = args.getOrElse("-m", "5").toInt
        strategy_id = args.getOrElse("-strategy", "baseline")
    }

    def init_strategies() = {
        strategies = Array[Repeatable]()
        val strategy: Strategy = if ("anygrad".equals(strategy_id)) {
            new Anygrad()
        }
        else if ("anygrad_sa".equals(strategy_id)) {
            new AnyGradSelectAll()
        }
        else {
            new Baseline
        }
        strategy.m = m
        strategies :+= strategy
    }
}