import numpy as np
import pandas as pd
from typing import List


def default_snapshot(default_score: float):
    return Snapshot(value=default_score, total_iterations=0, global_time=0.0, time_on_target=0.0,
                    iterations_on_target=0, incremental_iterations=0,
                    t_switch=np.nan, t1=np.nan, derivation_1st=np.nan, derivation_2nd=np.nan, efficiency=np.nan)


class Snapshot:
    def __init__(self, value: float,
                 total_iterations: int, global_time: float, time_on_target: float,
                 iterations_on_target: int, incremental_iterations: int,
                 derivation_1st: float, derivation_2nd: float,
                 t_switch: float, t1: float, efficiency: float):
        self.value = value
        self.total_iterations = total_iterations
        self.global_time = global_time
        self.time_on_target = time_on_target
        self.incremental_iterations = incremental_iterations
        self.iterations_on_target = iterations_on_target
        self.t_switch = t_switch
        self.t1 = t1
        self.derivation_1st = derivation_1st
        self.derivation_2nd = derivation_2nd
        self.efficiency = efficiency


def snapshots_to_df(snapshots: List[List[List[Snapshot]]]) -> pd.DataFrame:
    df = []
    cols = ["rep", "context_change", "target", "value", "m", "M",
            "total_iterations", "total_time", "time_on_target", "t_switch", "t1",
            "derivation_1st", "derivation_2nd", "efficiency"]
    for rep, results in enumerate(snapshots):
        for context_change, snaps in enumerate(results):
            for target, snap in enumerate(snaps):
                new_res = [
                    rep, context_change, "Target {}".format(int(target) + 1), snap.value,
                    snap.incremental_iterations, snap.iterations_on_target, snap.total_iterations,
                    snap.global_time, snap.time_on_target, snap.t_switch, snap.t1,
                    snap.derivation_1st, snap.derivation_2nd, snap.efficiency
                ]
                df.append(new_res)
    df = pd.DataFrame(df, index=None, columns=cols)
    return df


