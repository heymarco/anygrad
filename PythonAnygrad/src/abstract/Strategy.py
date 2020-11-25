from time import process_time
from abc import ABC, abstractmethod
import sys

import numpy as np
from typing import List

from src.ComputationOverheadTracker import ComputationOverheadTracker
from src.PerformanceObserver import PerformanceObserver
from src.utils.Snapshot import default_snapshot, Snapshot
from src.abstract.IterativeAlgorithm import IterativeAlgorithm
from src.utils.helper import wait_nonblocking


from src.utils.plotter import Plotter


class Strategy(ABC):

    def __init__(self, name: str,
                 algorithms: List[IterativeAlgorithm],
                 iterations: int,
                 burn_in_phase_length: int,
                 plotter: Plotter,
                 plotting_index: int,
                 sleep: float = 0.0):
        self.name = name
        self.algorithms = algorithms
        self.iterations = iterations
        self.burn_in_phase_length = burn_in_phase_length
        self.sleep = sleep
        self.plotter = plotter
        self.plotting_index = plotting_index

    @abstractmethod
    def get_m(self, derivation_1st: float, derivation_2nd: float, t_switch: float, t1: float):
        pass

    @abstractmethod
    def select_active_targets(self, targets: List[int], efficiency_list: List[float]):
        pass

    def burn_in_phase_finished(self, round):
        return round >= self.burn_in_phase_length

    def run(self, data, targets):
        # essential
        m_list = [self.iterations for _ in range(len(targets))]
        efficiency_list = [sys.float_info.max for _ in range(len(targets))]
        active_targets = list(range(len(targets)))

        # performance tracking
        timer = ComputationOverheadTracker(num_targets=len(targets))
        performance_observer = PerformanceObserver(num_targets=len(targets))

        # evaluation
        total_iterations = 0
        round = 0
        total_round_overhead = 0.0
        t_start = process_time()
        t_round = t_start
        results = [[default_snapshot()] for _ in range(len(targets))]
        # for i, alg in enumerate(self.algorithms):
        #     alg.partial_fit(self.__get_data__(data, at=0)[:alg.batch_size], 1)  # do one initial fit
        #     untrained_norm = alg.validate(X=self.__get_data__(data, at=0))
        #     results[i][0].value = untrained_norm
        # del untrained_norm
        best_snapshots = []
        timer.init_time()

        while len(active_targets):
            iterating_start = process_time()
            round += 1
            for i in active_targets:
                # improve target and measure performance
                last_snapshot = results[i][-1]
                current_data = self.__get_data__(data, at=i)
                m = m_list[i]
                alg_duration = self.algorithms[i].partial_fit(current_data, num_iterations=m)
                utility = self.algorithms[i].validate(current_data)
                wait_nonblocking(duration=self.sleep)

                # update performance metrics
                timer.update_time_model_parameters(target=i, alg_duration=alg_duration, m=m, delta=1.0)
                timer.set_signal()
                total_iterations += m
                total_iterations_on_target = last_snapshot.iterations_on_target + m
                performance_observer.enqueue((total_iterations_on_target, utility), at=i, queue_max_size=5)

                # update m and take snapshot
                t_switch, t1 = timer.get_time_model_for_target(i)
                if self.burn_in_phase_finished(round):
                    derivation_1st = performance_observer.get_1st_derivation_approximation(for_target=i)
                    derivation_2nd = performance_observer.get_2nd_derivation_approximation(for_target=i)
                    m_list[i] = self.get_m(derivation_1st=derivation_1st, derivation_2nd=derivation_2nd,
                                           t_switch=t_switch, t1=t1)
                    efficiency_list[i] = self.efficiency(t_switch=t_switch, t1=t1,
                                                         derivation_1st=derivation_1st, derivation_2nd=derivation_2nd,
                                                         m_opt=m_list[i])

                snapshot = Snapshot(value=utility, total_iterations=total_iterations,
                                    time_on_target=last_snapshot.time_on_target + alg_duration,
                                    global_time=process_time() - t_start,
                                    iterations_on_target=total_iterations_on_target,
                                    incremental_iterations=m,
                                    t_switch=t_switch, t1=t1)
                results[i].append(snapshot)
                # plotting
                if not len(best_snapshots) or snapshot.value >= best_snapshots[-1].value:
                    best_snapshots.append(snapshot)
            iterating_duration = process_time() - iterating_start
            round_processing_time = process_time() - t_round - iterating_duration
            t_round = process_time()
            timer.track_round_overhead_per_target(round_processing_time / len(active_targets))
            total_round_overhead += timer.get_total_round_overhead(num_active_targets=len(active_targets))
            if self.burn_in_phase_finished(round):
                active_targets = self.select_active_targets(targets=targets, efficiency_list=efficiency_list)
        x = [snap.global_time for snap in best_snapshots][1:]
        y = [snap.value for snap in best_snapshots][1:]
        self.plotter.add_to_subplot(x, y, ax_index=0, color_index=self.plotting_index)
        self.__print_statistics__(process_time() - t_start, total_round_overhead)
        return results

    def efficiency(self, t_switch: float, t1: float, derivation_1st: float, derivation_2nd: float, m_opt):
        eff = (derivation_1st * m_opt + 0.5*derivation_2nd * m_opt**2) / (t_switch + m_opt * t1)
        return eff

    def repeat(self, data, targets, num_repetitions: int):
        return [self.run(data, targets) for _ in range(num_repetitions)]

    def get_m_opt(self, derivation_1st: float, derivation_2nd: float, t_switch: float, t1: float):
        a = derivation_1st
        b = 0.5 * derivation_2nd
        c = t_switch
        d = t1
        if b == 0 or d == 0:
            return self.iterations
        under_root = b ** 2 * c ** 2 - a * b * c * d
        if under_root < 0:
            return 1
        m_opt = -(np.sqrt(under_root) + b * c) / (b * d)
        if np.isnan(m_opt):
            return self.iterations
        if m_opt < 0:
            print(a, b, c, d)
        if m_opt > 200:
            return 200
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
        if len(data) == 1: return data[0]
        else: return data[at]
