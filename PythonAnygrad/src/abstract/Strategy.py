import copy
from abc import ABC, abstractmethod
from time import process_time

import numpy as np
from typing import List

from src.ComputationOverheadTracker import ComputationOverheadTracker
from src.PerformanceObserver import PerformanceObserver
from src.abstract.PerformanceProfile import PerformanceProfile
from src.utils.snapshot import default_snapshot, Snapshot
from src.abstract.IterativeAlgorithm import IterativeAlgorithm
from src.utils.helper import wait_nonblocking


class Strategy(ABC):

    def __init__(self, name: str,
                 algorithms: List[IterativeAlgorithm],
                 iterations: int,
                 burn_in_phase_length: int,
                 performance_profile_class,
                 sleep: float = 0.0):
        self.name = name
        self.algorithms = algorithms
        self.iterations = iterations
        self.burn_in_phase_length = burn_in_phase_length
        self.sleep = sleep
        self.performance_profile_class = performance_profile_class

    @abstractmethod
    def get_m(self,
              derivation_1st: float, derivation_2nd: float,
              t_switch: float, t1: float,
              max_iterations: int) -> int:
        pass

    @abstractmethod
    def select_active_targets(self, targets: List[int], efficiency_list: List[float]) -> List[int]:
        pass

    def burn_in_phase_finished(self, round):
        return round > (self.burn_in_phase_length - 1)

    def run(self, train_data, val_data, targets, m_max: int, deadline_seconds: float = 120):
        # essential
        m_list = [self.iterations for _ in range(len(targets))]
        efficiency_list = [np.nan for _ in range(len(targets))]
        active_targets = list(range(len(targets)))


        # performance tracking
        timer = ComputationOverheadTracker(num_targets=len(targets))
        performance_profile = self.performance_profile_class()

        default_scores = [alg.warm_up(self.__get_data__(val_data, at=i)) for i, alg in enumerate(self.algorithms)]
        results = [[default_snapshot(default_score=default_scores[i]) for i in range(len(targets))]]

        # evaluation
        total_iterations = 0
        round = 0
        total_round_overhead = 0.0
        t_start = process_time()
        t_round = t_start
        [alg.set_start() for alg in self.algorithms]
        while len(active_targets) and process_time() - t_start < deadline_seconds:
            iterating_start = process_time()
            round += 1
            for i in active_targets:
                if self.algorithms[i].should_terminate():
                    continue
                results.append(copy.deepcopy(results[-1]))
                # improve target and measure performance
                last_snapshot = results[-1][i]
                current_train_data = self.__get_data__(train_data, at=i)
                current_val_data = self.__get_data__(val_data, at=i)
                m = m_list[i]
                alg_duration, m = self.algorithms[i].partial_fit(current_train_data, num_iterations=m)
                utility = self.algorithms[i].validate(current_val_data)
                wait_nonblocking(duration=self.sleep)

                # update performance metrics
                timer.update_time_model_parameters(target=i, alg_duration=alg_duration, m=m)
                total_iterations += m
                total_iterations_on_target = last_snapshot.iterations_on_target + m

                # update m and take snapshot
                t_switch, t1 = timer.get_time_model_for_target(i)
                d1, d2 = np.nan, np.nan
                quality = performance_profile.value(total_iterations_on_target)
                if self.burn_in_phase_finished(round):
                    d1 = performance_profile.first_derivation(total_iterations_on_target)
                    d2 = performance_profile.second_derivation(total_iterations_on_target)
                    m_list[i] = self.get_m(derivation_1st=d1, derivation_2nd=d2,
                                           t_switch=t_switch, t1=t1, max_iterations=m_max)
                    efficiency_list[i] = self.efficiency(t_switch=t_switch, t1=t1,
                                                         derivation_1st=d1, derivation_2nd=d2,
                                                         m_opt=m_list[i])

                # Update results
                global_time = process_time() - t_start
                for result_index in range(len(results[-1])):
                    # update timestamps of all copied results
                    results[-1][result_index].global_time = global_time
                    results[-1][result_index].total_iterations = total_iterations
                    results[-1][result_index].round = round
                # replace snapshot for newly improved target
                snapshot = Snapshot(value=utility, round=round, total_iterations=total_iterations,
                                    time_on_target=last_snapshot.time_on_target + alg_duration,
                                    global_time=global_time,
                                    iterations_on_target=total_iterations_on_target,
                                    incremental_iterations=m,
                                    t_switch=t_switch, t1=t1,
                                    derivation_1st=d1, derivation_2nd=d2,
                                    efficiency=efficiency_list[i], quality=quality)
                results[-1][i] = snapshot
            iterating_duration = process_time() - iterating_start
            round_processing_time = process_time() - t_round - iterating_duration
            t_round = process_time()
            total_round_overhead += round_processing_time
            if self.burn_in_phase_finished(round) or self.name.startswith("Baseline"):
                active_targets = self.select_active_targets(targets=targets, efficiency_list=efficiency_list)
        self.__print_statistics__(process_time() - t_start, total_round_overhead)
        return results

    def efficiency(self, t_switch: float, t1: float, derivation_1st: float, derivation_2nd: float, m_opt):
        eff = (derivation_1st * m_opt + 0.5*derivation_2nd * m_opt**2) / (t_switch + m_opt * t1)
        return eff

    def get_m_opt(self,
                  derivation_1st: float, derivation_2nd: float,
                  t_switch: float, t1: float,
                  max_iterations: int):
        a = derivation_1st
        b = 0.5 * derivation_2nd
        c = t_switch
        d = t1
        if b == 0 or d == 0:
            return self.iterations
        under_root = b ** 2 * c ** 2 - a * b * c * d
        if under_root < 0:
            return self.iterations
        m_opt = -(np.sqrt(under_root) + b * c) / (b * d)
        if np.isnan(m_opt):
            return self.iterations
        if m_opt < 0:
            print(a, b, c, d)
        if m_opt > max_iterations:
            return max_iterations
        return max(1, int(np.ceil(m_opt)))

    def select_efficient_targets(self, targets: List[int], efficiency_list: List[float]):
        active_targets = [i for i in targets if not self.algorithms[i].should_terminate()]
        if len(active_targets) <= 1:
            return active_targets

        min_gradient = np.nanmin(efficiency_list)
        max_gradient = np.nanmax(efficiency_list)
        if min_gradient == max_gradient:
            return active_targets

        active_targets = [
            target for target in active_targets if
            (efficiency_list[target] - min_gradient) / (max_gradient - min_gradient) >= np.random.uniform()]
        return active_targets

    def __print_statistics__(self, total_runtime, total_round_overhead):
        print("Total runtime of {} = {}".format(self.name, total_runtime))
        print("Total round overhead of {} = {}".format(self.name, total_round_overhead))

    def __get_data__(self, data, at: int):
        if data is None:
            # for algorithms which don't use data. E.g. A*
            return data
        elif len(data) == 1:
            return data[0]
        else:
            return data[at]
