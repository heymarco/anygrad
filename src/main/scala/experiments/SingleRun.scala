package experiments

import strategies.anygrad_variations.{AnyGradSelectProbability, AnygradEmpiricalInfo}
import strategies.{AnyGrad, AnyGradSelectAll, Baseline}
import traits.{Experiment, Repeatable, Strategy}


class SingleRun extends Experiment {
    var m = 8
    var strategy_id = "baseline"

    override def setup(args: Map[String, String]): Unit = {
        super.setup(args)
        m = args.getOrElse("-m", "10").toInt
        strategy_id = args.getOrElse("-strategy", "baseline")
    }

    def init_strategies() = {
        strategies = Array[Repeatable]()
        val strategy: Strategy = if ("anygrad".equals(strategy_id)) {
            new AnyGrad()
        }
        else if ("anygrad_sa".equals(strategy_id)) {
            new AnyGradSelectAll
        }
        else if ("anygrad_sp".equals(strategy_id)) {
            new AnyGradSelectProbability
        }
        else if ("anygrad_ei".equals(strategy_id)) {
            new AnygradEmpiricalInfo
        }
        else {
            new Baseline
        }
        strategy.m = m
        strategies :+= strategy
    }
}