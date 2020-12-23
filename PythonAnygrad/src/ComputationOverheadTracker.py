from time import process_time
import numpy as np


class ComputationOverheadTracker:

    def __init__(self, num_targets: int):
        self.num_targets = num_targets
        self.round_overhead_queue = np.empty(shape=0, dtype=float)
        self.time_model_queues = [np.empty(shape=(0, 2), dtype=float) for _ in range(num_targets)]
        self.start_time = np.nan
        self.end_time = np.nan

    def init_time(self):
        self.start_time = process_time()
        self.end_time = process_time()

    def __enqueue_switching_time__(self, new_value: (float, float),
                                   at: int,
                                   queue_max_size: int = 10):
        self.time_model_queues[at] = np.append(self.time_model_queues[at], [np.array(new_value)], axis=0)
        if len(self.time_model_queues[at]) > queue_max_size:
            self.time_model_queues[at] = self.time_model_queues[at][-queue_max_size:]

    def track_round_overhead(self, overhead: float,
                             queue_max_size: int = 10):
        self.round_overhead_queue = np.append(self.round_overhead_queue, overhead)
        if len(self.round_overhead_queue) > queue_max_size:
            self.round_overhead_queue = self.round_overhead_queue[-queue_max_size:]

    def update_time_model_parameters(self, target: int,
                                     alg_duration: float,
                                     m: int, delta: float = 1.0):
        assert not np.isnan(self.end_time) and not np.isnan(self.start_time)
        total = self.end_time - self.start_time
        t_switch = total - alg_duration
        t1 = alg_duration / m
        # add delta
        delta_diff = t1 * (1 - delta)
        t1 -= delta_diff
        t_switch += delta_diff
        self.__enqueue_switching_time__(new_value=(t_switch, t1), at=target)

    def get_time_model_for_target(self, at: int, num_active_targets: int):
        if not len(self.time_model_queues[at]):
            return np.nan, np.nan
        t_switch = self.time_model_queues[at][:, 0]
        t1 = self.time_model_queues[at][:, 1]
        t_switch = t_switch.sum() / len(t_switch) + self.get_round_overhead_per_target(num_targets=num_active_targets)
        t1 = t1.sum() / len(t1)
        return t_switch, t1

    def get_round_overhead_per_target(self, num_targets: int):
        return self.get_total_round_overhead() / num_targets

    def get_total_round_overhead(self):
        if not len(self.round_overhead_queue):
            return 0.0
        return self.round_overhead_queue.sum() / len(self.round_overhead_queue)

    def set_signal(self):
        self.start_time = self.end_time
        self.end_time = process_time()
