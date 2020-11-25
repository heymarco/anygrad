import json
import numpy as np
from typing import List


def default_snapshot():
    return Snapshot(value=np.nan, total_iterations=0, global_time=0.0, time_on_target=0.0,
                    iterations_on_target=0, incremental_iterations=0,
                    t_switch=np.nan, t1=np.nan)


def snapshot_from_ndarray(arr):
    return Snapshot(value=arr[0], total_iterations=arr[1], time=arr[2],
                    incremental_iterations=arr[3], t_switch=arr[4], t1=arr[5])


class Snapshot:

    def __init__(self, value: float,
                 total_iterations: int, global_time: float, time_on_target: float, iterations_on_target: int, incremental_iterations: int,
                 t_switch: float, t1: float):
        self.value = value
        self.total_iterations = total_iterations
        self.global_time = global_time
        self.time_on_target = time_on_target
        self.incremental_iterations = incremental_iterations
        self.iterations_on_target = iterations_on_target
        self.t_switch = t_switch
        self.t1 = t1

    def to_numpy(self):
        return np.array([
            self.value, self.total_iterations, self.global_time, self.time_on_target,
            self.iterations_on_target, self.incremental_iterations, self.t_switch, self.t1
        ])

    def to_dict(self):
        data = {
            "result": self.value,
            "iterations": self.total_iterations,
            "duration": self.global_time,
            "time_on_target": self.time_on_target,
            "M": self.iterations_on_target,
            "m": self.incremental_iterations,
            "tcs": self.t_switch,
            "t1": self.t1
        }
        return data


def snapshots_to_json(snapshots: List[List[List[Snapshot]]]):
    num_targets = len(snapshots[0][0])
    for rep in snapshots:
        for target_res in rep:
            pass


# result_row.append(Map[String, Double](
#                             "result" -> solution._1,
#                             "variance" -> variance(solution._3),
#                             "quality" -> quality,
#                             "utility" -> utility,
#                             "m" -> m,
#                             "M" -> solution._2,
#                             "iterations" -> iterations,
#                             "duration" -> time,
#                             "tcs" -> t_cs,
#                             "t1" -> t_1
#                         ))