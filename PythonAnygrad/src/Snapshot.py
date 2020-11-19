import numpy as np


def default_snapshot():
    return Snapshot(value=np.nan, quality=0.0, utility=0.0,
                    total_iterations=0, time=0.0, incremental_iterations=0,
                    t_switch=np.nan, t1=np.nan)


class Snapshot:

    def __init__(self, value: float, quality: float, utility: float,
                 total_iterations: int, time: float, incremental_iterations: int,
                 t_switch: float, t1: float):
        self.value = value
        self.quality = quality
        self.utility = utility
        self.total_iterations = total_iterations
        self.time = time
        self.incremental_iterations = incremental_iterations
        self.t_switch = t_switch
        self.t1 = t1

    def to_numpy(self):
        return np.array([
            self.value, self.quality, self.utility, self.total_iterations,
            self.time, self.incremental_iterations, self.t_switch, self.t1
        ])
