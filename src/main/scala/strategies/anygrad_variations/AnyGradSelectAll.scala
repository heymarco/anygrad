package strategies

import scala.collection.mutable.ArrayBuffer
import scala.math.{max, pow, sqrt}
import objects.bound.{Chernoff, Hoeffding}
import objects.estimators.MCDE
import traits.Strategy
import objects.utility.{Identity, None}
import utils.types.{Snapshot, Solution}


class AnyGradSelectAll extends Strategy {
    def name: String = s"anygrad-sa"
}