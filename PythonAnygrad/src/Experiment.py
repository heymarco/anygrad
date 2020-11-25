from typing import List
import numpy as np
from sklearn.preprocessing import scale, MinMaxScaler

from src.strategies import Anygrad, Baseline, AnygradSelectAll, AnygradOnlySelection
from src.abstract.Strategy import Strategy
from src.algorithms import MiniBatchKMeansAlg, MLPAlg
from src.utils.plotter import Plotter

baseline_iterations = 20


class Experiment:

    def __init__(self, strategies: List[Strategy],
                 data: List[np.ndarray],
                 targets: List[int],
                 num_reps: int,
                 parallel: bool,
                 file_dir: str,
                 name: str,
                 plotter: Plotter,
                 target_dir: str = "./save"):
        self.data = data
        self.strategies = strategies
        self.num_reps = num_reps
        self.parallel = parallel
        self.file_dir = file_dir
        self.name = name
        self.target_dir = target_dir
        self.targets = targets
        self.plotter = plotter

    def run(self):
        results = {}
        for strategy in self.strategies:
            results[strategy.name] = strategy.repeat(self.data, self.targets, self.num_reps)
        self.plotter.add_legend([s.name for s in self.strategies])
        self.plotter.set_ax_labels(("time", "maximum score"))
        self.plotter.save("fig.pdf")
        # TODO: evaluate, print, or store the results


def experiment_factory(experiment_name: str, data: List[np.ndarray], targets: List[int],
                       num_reps: int, parallel: bool, file_dir: str, target_dir: str, sleep: float):
    num_targets = len(targets)
    random_seed = np.random.randint(0, 100)
    strategies = []
    if experiment_name.startswith("kmeans"):
        data = [scale(d) for d in data]
        plotter = Plotter(num_targets=num_targets, num_colors=6, num_rows=1, num_cols=1,
                          sharex="all", sharey="all")
        num_clusters = [2 + i for i in range(num_targets)]
        np.random.shuffle(num_clusters)
        if experiment_name == "kmeans_batch":
            j = 0
            iterations = 10
            algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i], seed=random_seed)
                          for i in range(num_targets)]
            strategies.append(Baseline("Baseline (round robin, m=1)",
                                       algorithms=algorithms,
                                       iterations=1,
                                       burn_in_phase_length=3, sleep=sleep,
                                       plotter=plotter, plotting_index=j))
            j += 1
            algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i], seed=random_seed)
                          for i in range(num_targets)]
            strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations),
                                       algorithms=algorithms,
                                       iterations=baseline_iterations,
                                       burn_in_phase_length=3, sleep=sleep,
                                       plotter=plotter, plotting_index=j))
            j += 1
            algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i], seed=random_seed)
                          for i in range(num_targets)]
            strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations * 5),
                                       algorithms=algorithms,
                                       iterations=baseline_iterations * 5,
                                       burn_in_phase_length=3, sleep=sleep,
                                       plotter=plotter, plotting_index=j))
            j += 1
            algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i], seed=random_seed)
                          for i in range(num_targets)]
            strategies.append(AnygradSelectAll("Anygrad (no target selection)", algorithms=algorithms,
                                               iterations=iterations,
                                               burn_in_phase_length=3, sleep=sleep,
                                               plotter=plotter, plotting_index=j))
            j += 1
            algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i], seed=random_seed)
                          for i in range(num_targets)]
            strategies.append(AnygradOnlySelection("Anygrad (m={})".format(baseline_iterations),
                                                   algorithms=algorithms,
                                                   iterations=baseline_iterations,
                                                   burn_in_phase_length=10, sleep=sleep,
                                                   plotter=plotter, plotting_index=j))
            j += 1
            algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i], seed=random_seed)
                          for i in range(num_targets)]
            strategies.append(Anygrad("Anygrad (full)", algorithms=algorithms,
                                      iterations=iterations,
                                      burn_in_phase_length=3, sleep=sleep,
                                      plotter=plotter, plotting_index=j))
    elif experiment_name == "mlp":
        data = [MinMaxScaler().fit_transform(d) for d in data]
        plotter = Plotter(num_targets=num_targets, num_colors=5, num_rows=1, num_cols=1, sharex="all", sharey="all")
        dimensionality = int(data[0].shape[-1] * 0.5)
        j = 0
        lr_max = 0.05
        lr_min = 0.00001
        iterations = 50
        lr = [lr_min + (lr_max - lr_min) * float(i) / (num_targets - 1) for i in range(num_targets)]
        np.random.shuffle(lr)
        algorithms = [MLPAlg(neurons_hidden=dimensionality, learning_rate=lr[i], random_state=random_seed)
                      for i in range(num_targets)]
        strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations),
                                   algorithms=algorithms,
                                   iterations=baseline_iterations,
                                   burn_in_phase_length=3, sleep=0.0,
                                   plotter=plotter, plotting_index=j))
        j += 1
        algorithms = [MLPAlg(neurons_hidden=dimensionality, learning_rate=lr[i], random_state=random_seed)
                      for i in range(num_targets)]
        strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations*10),
                                   algorithms=algorithms,
                                   iterations=baseline_iterations*10,
                                   burn_in_phase_length=3, sleep=0.0,
                                   plotter=plotter, plotting_index=j))
        j += 1
        algorithms = [MLPAlg(neurons_hidden=dimensionality, learning_rate=lr[i], random_state=random_seed)
                      for i in range(num_targets)]
        strategies.append(AnygradSelectAll("Anygrad (no target selection)",
                                           algorithms=algorithms,
                                           iterations=iterations,
                                           burn_in_phase_length=3, sleep=0.0,
                                           plotter=plotter, plotting_index=j))
        j += 1
        algorithms = [MLPAlg(neurons_hidden=dimensionality, learning_rate=lr[i], random_state=random_seed)
                      for i in range(num_targets)]
        strategies.append(AnygradOnlySelection("Anygrad (m={})".format(baseline_iterations),
                                               algorithms=algorithms,
                                               iterations=baseline_iterations,
                                               burn_in_phase_length=3, sleep=0.0,
                                               plotter=plotter, plotting_index=j))
        j += 1
        algorithms = [MLPAlg(neurons_hidden=dimensionality, learning_rate=lr[i], random_state=random_seed)
                      for i in range(num_targets)]
        strategies.append(Anygrad("Anygrad (full)", algorithms=algorithms,
                                  iterations=iterations,
                                  burn_in_phase_length=3, sleep=0.0,
                                  plotter=plotter, plotting_index=j))
    else:
        assert "Error, no valid experiment_name provided."
    return Experiment(name=experiment_name, strategies=strategies, data=data, targets=targets,
                      num_reps=num_reps, parallel=parallel, plotter=plotter,
                      file_dir=file_dir, target_dir=target_dir)
    assert "Error, no valid experiment_name provided."
