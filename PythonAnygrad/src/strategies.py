from typing import List

from src.abstract.Strategy import Strategy


class Anygrad(Strategy):
    def get_m(self, derivation_1st: float, derivation_2nd: float, t_switch: float, t1: float, max_iterations: int):
        m_opt = self.get_m_opt(derivation_1st, derivation_2nd, t_switch, t1, max_iterations)
        return m_opt

    def select_active_targets(self, targets: List[int], efficiency_list: List[float]):
        return self.select_efficient_targets(targets, efficiency_list)


class AnygradSelectAll(Strategy):
    def get_m(self, derivation_1st: float, derivation_2nd: float, t_switch: float, t1: float, max_iterations: int):
        return self.get_m_opt(derivation_1st, derivation_2nd, t_switch, t1, max_iterations)

    def select_active_targets(self, targets: List[int], efficiency_list: List[float]):
        return [i for i in targets if not self.algorithms[i].should_terminate()]


class AnygradOnlySelection(Strategy):
    def get_m(self, derivation_1st: float, derivation_2nd: float, t_switch: float, t1: float, max_iterations: int):
        return self.iterations

    def select_active_targets(self, targets: List[int], efficiency_list: List[float]):
        return self.select_efficient_targets(targets, efficiency_list)


class Baseline(Strategy):
    def get_m(self, derivation_1st: float, derivation_2nd: float, t_switch: float, t1: float, max_iterations: int):
        return self.iterations

    def select_active_targets(self, targets: List[int], efficiency_list: List[float]):
        return [i for i in targets if not self.algorithms[i].should_terminate()]
