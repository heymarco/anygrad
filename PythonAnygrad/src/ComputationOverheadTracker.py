from time import process_time_ns
import numpy as np


class ComputationOverheadTracker:

    def __init__(self, num_targets: int):
        self.num_targets = num_targets
        self.round_overhead_queue = np.empty(shape=0, dtype=float)
        self.time_model_queues = [np.empty(shape=(0, 2), dtype=float) for _ in range(num_targets)]
        self.start_time = np.nan
        self.end_time = np.nan

    def __enqueue_switching_time__(self, new_value: (float, float),
                                   at: int,
                                   queue_max_size: int = 10):
        self.time_model_queues[at] = np.append(self.time_model_queues[at], np.array(new_value), axis=0)
        if len(self.time_model_queues[at]) > queue_max_size:
            self.time_model_queues[at] = self.time_model_queues[at][-queue_max_size:]

    def track_round_overhead_per_target(self, overhead_per_target: float,
                                        queue_max_size: int = 30):
        self.round_overhead_queue = np.append(self.round_overhead_queue, overhead_per_target)
        if len(self.round_overhead_queue) > queue_max_size:
            self.round_overhead_queue = self.round_overhead_queue[-queue_max_size:]

    def update_time_model_parameters(self, target: int,
                                     alg_duration: float,
                                     m: int):
        if np.isnan(self.end_time) or np.isnan(self.start_time):
            assert "Error using computation overhead tracker. Have you called `set_signal_start` and `set_signal_end`?"
        total = self.end_time - self.start_time
        t_switch = total - alg_duration
        t1 = alg_duration / m
        self.__enqueue_switching_time__(new_value=(t_switch, t1), at=target)

    def get_time_model_for_target(self, at: int):
        t_switch = self.time_model_queues[at][0, :]
        t1 = self.time_model_queues[at][1, :]
        t_switch = t_switch.sum() / len(t_switch) + self.get_round_overhead_per_target()
        t1 = t1.sum() / len(t1)
        return t_switch, t1

    def get_round_overhead_per_target(self):
        return self.round_overhead_queue.sum() / len(self.round_overhead_queue)

    def get_total_round_overhead(self, num_active_targets: int):
        return self.get_round_overhead_per_target() * num_active_targets

    def set_signal_start(self):
        self.start_time = process_time_ns()

    def set_signal_end(self):
        self.end_time = process_time_ns()




