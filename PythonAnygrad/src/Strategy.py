from time import process_time_ns
from abc import ABC, abstractmethod

import numpy as np

from src.ComputationOverheadTracker import ComputationOverheadTracker
from src.Snapshot import Snapshot, default_snapshot


class Strategy(ABC):

    def __init__(self, algorithms,
                 quality_measures,
                 utility_measures,
                 iterations: int,
                 burn_in_phase_length: int,
                 sleep: float = 0.0):
        self.algorithms = algorithms
        self.quality_measures = quality_measures
        self.utility_measures = utility_measures
        self.iterations = iterations
        self.burn_in_phase_length = burn_in_phase_length
        self.sleep = sleep

    def get_m(self, derivation_1st: float, derivation_2nd: float, t_switch: float, t1: float):
        a = derivation_1st
        b = derivation_2nd
        c = t_switch
        d = t1
        m_opt = (np.sqrt(b**2*c**2 - a*b*c*d) - b*c) / (b*d)
        return max(1, int(np.ceil(m_opt)))

    @abstractmethod
    def setup(self, args):
        pass

    @abstractmethod
    def select_active_targets(self):
        pass

    def burn_in_phase_finished(self, round):
        return round >= self.burn_in_phase_length

    def run(self, targets, termination_criteria):
        results = []
        timer = ComputationOverheadTracker(num_targets=len(targets))
        active_targets = list(range(len(targets)))
        total_iterations = 0
        round = 0
        default_solution = (0.0, 0, (0, 0.0, 0.0))
        t_start = process_time_ns()
        total_round_overhead = 0.0
        round_timestamp = process_time_ns()
        results = np.array([[default_snapshot()] for _ in range(len(targets))])

        while len(active_targets):
            iterating_start = process_time_ns()
            round_results = []
            for i in active_targets:
                timer.set_signal_start()
                current_target = targets[i]
                last_result = results[i][-1]
                t_switch, t_1 = timer.get_time_model_for_target(i)
                # todo: derivation 1 and 2 --> implement performance observer
                # todo: iterations = if burn_in_phase_finished(round) ...
