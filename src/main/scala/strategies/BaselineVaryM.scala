package strategies

import traits.Strategy
import utils.types.Solution


class BaselineVaryM extends Strategy {

    def name: String = { s"baseline-vary-m"}
    private var counter: Int = 0
    val m_list = scala.util.Random.shuffle(List.range(1, 100, 2))

    override def get_m(solution: Solution, t_cs: Double, t_1: Double): Int = {
        val m = m_list(counter)
        counter = counter + 1
        counter = counter % m_list.length
        m
    }
}