package experiments

import strategies.{AnyGradSelectAll, Baseline, Anygrad}
import traits.{Experiment, Repeatable}


class SingleRun extends Experiment {
    var m = 8

    override def setup(args: Map[String, String]): Unit = {
        super.setup(args)
        m = args.getOrElse("-m", "5").toInt
    }

    def init_strategies() = {
        strategies = Array[Repeatable]()
        val strategy = if (m == 0) {
            new Anygrad
        }
        else {
            new Baseline
        }
        if (m > 0) {
            strategy.m = m
        }
        strategies :+= strategy
    }
}