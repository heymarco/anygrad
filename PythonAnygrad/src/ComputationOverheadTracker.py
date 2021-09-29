from time import process_time
import numpy as np


class ComputationOverheadTracker:

    def __init__(self, num_targets: int):
        self.num_targets = num_targets
        self.round_overhead_queue = np.empty(shape=0, dtype=float)
        self.time_model_queues = [np.empty(shape=(0, 2), dtype=float) for _ in range(num_targets)]
        self.previous_timestamp = np.nan

    def __enqueue_switching_time__(self, new_value: (float, float),
                                   at: int,
                                   queue_max_size: int = 10):
        self.time_model_queues[at] = np.append(self.time_model_queues[at], [np.array(new_value)], axis=0)
        if len(self.time_model_queues[at]) > queue_max_size:
            self.time_model_queues[at] = self.time_model_queues[at][-queue_max_size:]

    def update_time_model_parameters(self, target: int,
                                     alg_duration: float,
                                     m: int):
        if np.isnan(self.previous_timestamp):
            self.previous_timestamp = process_time()
            return
        current_time = process_time()
        total = current_time - self.previous_timestamp
        self.previous_timestamp = current_time
        t1 = alg_duration / m
        t_switch = total - alg_duration
        if not (alg_duration > 0 and t_switch > 0):
            print(total, alg_duration, len(self.time_model_queues[target]))
        assert t1 > 0 and t_switch > 0
        self.__enqueue_switching_time__(new_value=(t_switch, t1), at=target)

    def get_time_model_for_target(self, at: int):
        if not len(self.time_model_queues[at]):
            return np.nan, np.nan
        t_switch = self.time_model_queues[at][:, 0]
        t1 = self.time_model_queues[at][:, 1]
        t_switch = t_switch.sum() / len(t_switch)
        t1 = t1.sum() / len(t1)
        return t_switch, t1
