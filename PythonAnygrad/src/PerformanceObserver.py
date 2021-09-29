from typing import List

import numpy as np
from scipy.stats import linregress


class PerformanceObserver:

    def __init__(self, num_targets):
        self.queues = [np.empty(shape=(0, 2), dtype=float) for _ in range(num_targets)]
        self.num_targets = num_targets
        self.coefficient1: List[float] = [np.nan for _ in range(num_targets)]
        self.coefficient2: List[float] = [np.nan for _ in range(num_targets)]
        self.queue_max_size: int = 10

    def enqueue(self, new_value: (float, float), at: int):
        self.queues[at] = np.append(self.queues[at], [np.array(new_value)], axis=0)
        if len(self.queues[at]) > self.queue_max_size:
            self.queues[at] = self.queues[at][-self.queue_max_size:]

    def get_1st_derivation_approximation(self, for_target: int):
        this_queue = self.queues[for_target]
        quality = this_queue[0:, 1]
        iterations = this_queue[0:, 0]
        coefficients = np.polyfit(iterations, quality, deg=2)
        self.coefficient1[for_target] = coefficients[1]
        self.coefficient2[for_target] = coefficients[0]
        return self.coefficient1[for_target]

    def get_2nd_derivation_approximation(self, for_target: int):
        return self.coefficient2[for_target]
