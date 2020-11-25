import numpy as np
from scipy.stats import linregress


class PerformanceObserver:

    def __init__(self, num_targets):
        self.queues = [np.empty(shape=(0, 2), dtype=float) for _ in range(num_targets)]
        self.num_targets = num_targets

    def enqueue(self, new_value: (float, float), at: int, queue_max_size: int = 10):
        self.queues[at] = np.append(self.queues[at], [np.array(new_value)], axis=0)
        if len(self.queues[at]) > queue_max_size:
            self.queues[at] = self.queues[at][-queue_max_size:]

    def get_1st_derivation_approximation(self, for_target: int):
        this_queue = self.queues[for_target]
        quality = this_queue[:, 1]
        iterations = this_queue[:, 0]
        slope = linregress(iterations, quality)[0]
        return max(0.0, slope)

    def get_2nd_derivation_approximation(self, for_target: int):
        this_queue = self.queues[for_target]
        quality = this_queue[:, 1]
        iterations = this_queue[:, 0]
        diff_quality = np.diff(quality)
        diff_iterations = np.diff(iterations)
        y = diff_quality / diff_iterations
        slope = linregress(iterations[1:], y)[0]
        return min(0.0, slope)
