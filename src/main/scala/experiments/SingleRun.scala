package experiments

import strategies.anygrad_variations.AnyGradSelectProbability
import strategies.{AnyGrad, AnyGradSelectAll, Baseline, BaselineVaryM}
import traits.{Experiment, Repeatable, Strategy}


class SingleRun extends Experiment {
    var m = 8
    private var strategy_id = ""
    val name = "Single run"

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
        else if ("baseline".equals(strategy_id)) {
            new Baseline
        }
        else if ("baseline-vary-m".equals(strategy_id)) {
            new BaselineVaryM
        }
        else {
            throw new Error("No valid strategy identifier provided")
        }
        strategy.m = m
        strategies :+= strategy
    }
}