import copy
import os
from typing import List
import torch
import torchvision
import torchvision.transforms as transforms
import numpy as np
from sklearn.datasets import fetch_openml
from sklearn.preprocessing import MinMaxScaler
from sklearn.model_selection import ParameterGrid

from src.strategies import Anygrad, Baseline, AnygradSelectAll, AnygradOnlySelection
from src.abstract.Strategy import Strategy
from src.algorithms import MiniBatchKMeansAlg, MLPAlg, ConvolutionalAEAlg
from src.utils.snapshot import snapshots_to_df
from src.utils.helper import set_random_state

baseline_iterations = 20


class Experiment:

    def __init__(self, strategies: List[Strategy],
                 train_data,
                 val_data,
                 targets: List[int],
                 num_reps: int,
                 parallel: bool,
                 name: str,
                 target_dir: str = "./save"):
        self.train_data = train_data
        self.val_data = val_data
        self.strategies = strategies
        self.num_reps = num_reps
        self.parallel = parallel
        self.name = name
        self.target_dir = target_dir
        self.targets = targets

    def run(self):
        random_seed = np.random.randint(low=0, high=100, size=self.num_reps)
        for strategy in self.strategies:
            copies = [copy.deepcopy(strategy) for _ in range(self.num_reps)]
            result = []
            for i, strat in enumerate(copies):
                set_random_state(random_seed[i])
                result.append(strat.run(self.train_data, self.val_data, self.targets))
            df = snapshots_to_df(result)
            df.to_csv(path_or_buf=os.path.join(self.target_dir, strategy.name + ".csv"), sep=";", index=False)
            del copies


def create_kmeans_experiment(num_targets: int, num_reps: int, target_dir: str, sleep: float = 0.0):
    X, _ = fetch_openml(name="cardiotocography", version=2, return_X_y=True)
    strategies = []
    scaler = MinMaxScaler()
    data = [scaler.fit_transform(X)]
    num_clusters = [2 + i for i in range(num_targets)]
    np.random.shuffle(num_clusters)
    j = 0
    iterations = 1
    algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i])
                  for i in range(num_targets)]
    strategies.append(Baseline("Baseline (round robin, m=1)",
                               algorithms=algorithms,
                               iterations=1,
                               burn_in_phase_length=3, sleep=sleep))
    j += 1
    algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i])
                  for i in range(num_targets)]
    strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations),
                               algorithms=algorithms,
                               iterations=baseline_iterations,
                               burn_in_phase_length=3, sleep=sleep))
    j += 1
    algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i])
                  for i in range(num_targets)]
    strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations * 5),
                               algorithms=algorithms,
                               iterations=baseline_iterations * 5,
                               burn_in_phase_length=3, sleep=sleep))
    j += 1
    algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i])
                  for i in range(num_targets)]
    strategies.append(AnygradSelectAll("Anygrad (no target selection)", algorithms=algorithms,
                                       iterations=iterations,
                                       burn_in_phase_length=3, sleep=sleep))
    j += 1
    algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i])
                  for i in range(num_targets)]
    strategies.append(AnygradOnlySelection("Anygrad (m={})".format(baseline_iterations),
                                           algorithms=algorithms,
                                           iterations=baseline_iterations,
                                           burn_in_phase_length=10, sleep=sleep))
    j += 1
    algorithms = [MiniBatchKMeansAlg(n_clusters=num_clusters[i])
                  for i in range(num_targets)]
    strategies.append(Anygrad("Anygrad (full)", algorithms=algorithms,
                              iterations=iterations,
                              burn_in_phase_length=3, sleep=sleep))
    return Experiment(name="kmeans_minibatch", strategies=strategies,
                      train_data=data, val_data=data,
                      targets=[i for i in range(num_targets)],
                      num_reps=num_reps, parallel=False, target_dir=target_dir)


def create_mlp_experiment(num_targets: int, num_reps: int, target_dir: str, sleep: float = 0.0):
    datasets = {
        "fmnist": 40996
    }
    X, _ = fetch_openml(data_id=datasets["fmnist"], return_X_y=True)
    np.random.shuffle(X)
    X = X[:4000]
    strategies = []
    scaler = MinMaxScaler()
    data = [scaler.fit_transform(X)]
    burn_in_phase_length = 3
    parameter_dict = {
        "lr": [0.001],
        "neurons": [
            [num_neurons for _ in range(num_layers)]
            for num_layers in [1] for num_neurons in [4, 20, 100]
        ]
    }
    data = [MinMaxScaler().fit_transform(d) for d in data]
    iterations = 3
    grid = ParameterGrid(parameter_dict)
    grid = list(grid)[:num_targets]
    j = 0
    algorithms = [MLPAlg(neurons_hidden=params["neurons"], learning_rate=params["lr"])
                  for params in grid]
    strategies.append(Baseline("Baseline (round robin, m={})".format(iterations),
                               algorithms=algorithms,
                               iterations=iterations,
                               burn_in_phase_length=burn_in_phase_length,
                               sleep=0.0))
    j += 1
    algorithms = [MLPAlg(neurons_hidden=params["neurons"], learning_rate=params["lr"])
                  for params in grid]
    strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations),
                               algorithms=algorithms,
                               iterations=baseline_iterations,
                               burn_in_phase_length=burn_in_phase_length,
                               sleep=0.0))
    j += 1
    algorithms = [MLPAlg(neurons_hidden=params["neurons"], learning_rate=params["lr"])
                  for params in grid]
    strategies.append(Baseline("Baseline (round robin, m={})".format(baseline_iterations * 5),
                               algorithms=algorithms,
                               iterations=baseline_iterations * 5,
                               burn_in_phase_length=burn_in_phase_length,
                               sleep=0.0))
    j += 1
    algorithms = [MLPAlg(neurons_hidden=params["neurons"], learning_rate=params["lr"])
                  for params in grid]
    strategies.append(AnygradSelectAll("Anygrad (no target selection)",
                                       algorithms=algorithms,
                                       iterations=iterations,
                                       burn_in_phase_length=burn_in_phase_length,
                                       sleep=0.0))
    j += 1
    algorithms = [MLPAlg(neurons_hidden=params["neurons"], learning_rate=params["lr"])
                  for params in grid]
    strategies.append(AnygradOnlySelection("Anygrad (m={})".format(baseline_iterations),
                                           algorithms=algorithms,
                                           iterations=baseline_iterations,
                                           burn_in_phase_length=burn_in_phase_length,
                                           sleep=0.0))
    j += 1
    algorithms = [MLPAlg(neurons_hidden=params["neurons"], learning_rate=params["lr"])
                  for params in grid]
    strategies.append(Anygrad("Anygrad (full)", algorithms=algorithms,
                              iterations=iterations,
                              burn_in_phase_length=burn_in_phase_length,
                              sleep=0.0))
    return Experiment(name="Multilayer Perceptron", strategies=strategies,
                      train_data=data, val_data=data,
                      targets=[i for i in range(num_targets)],
                      num_reps=num_reps, parallel=False,
                      target_dir=target_dir)


def create_cifar_experiment(num_targets: int, num_reps: int, target_dir: str, sleep: float = 0.0):
    # Converting data to torch.FloatTensor
    transform = transforms.ToTensor()

    # Download the training and test datasets
    train_data = torchvision.datasets.CIFAR10(root='data', train=True, download=True, transform=transform)
    val_data = torchvision.datasets.CIFAR10(root='data', train=False, download=True, transform=transform)

    # Prepare data loaders
    train_loader = torch.utils.data.DataLoader(train_data, batch_size=32, num_workers=0)
    val_loader = torch.utils.data.DataLoader(val_data, batch_size=32, num_workers=0)

    parameter_dict = {
        "lr": [0.001, 0.005, 0.01],
        "num_filters": [3, 6, 12]
    }
    grid = ParameterGrid(parameter_dict)
    grid = list(grid)[:num_targets]
    grid = grid[:num_targets]
    print(grid)

    iterations = 1
    baseline_iterations = [1, 2, 4]
    burn_in_phase_length = 3
    strategies = []
    j = 0
    for it in baseline_iterations:
        algorithms = [
            ConvolutionalAEAlg(num_channels=3, num_filters=params["num_filters"], learning_rate=params["lr"])
            for params in grid
        ]
        strategies.append(Baseline("Baseline (round robin, m={})".format(it),
                                   algorithms=algorithms,
                                   iterations=it,
                                   burn_in_phase_length=burn_in_phase_length,
                                   sleep=0.0))
        j += 1
    algorithms = [
        ConvolutionalAEAlg(num_channels=3, num_filters=params["num_filters"], learning_rate=params["lr"])
        for params in grid
    ]
    strategies.append(AnygradSelectAll("Anygrad (no target selection)",
                                       algorithms=algorithms,
                                       iterations=iterations,
                                       burn_in_phase_length=burn_in_phase_length,
                                       sleep=0.0))
    j += 1
    algorithms = [
        ConvolutionalAEAlg(num_channels=3, num_filters=params["num_filters"], learning_rate=params["lr"])
        for params in grid
    ]
    strategies.append(AnygradOnlySelection("Anygrad (m={})".format(150),
                                           algorithms=algorithms,
                                           iterations=150,
                                           burn_in_phase_length=burn_in_phase_length,
                                           sleep=0.0))
    j += 1
    algorithms = [
        ConvolutionalAEAlg(num_channels=3, num_filters=params["num_filters"], learning_rate=params["lr"])
        for params in grid
    ]
    strategies.append(Anygrad("Anygrad (full)", algorithms=algorithms,
                              iterations=iterations,
                              burn_in_phase_length=burn_in_phase_length,
                              sleep=0.0))
    return Experiment(name="Multilayer Perceptron", strategies=strategies,
                      train_data=[train_loader], val_data=[val_loader],
                      targets=[i for i in range(num_targets)],
                      num_reps=num_reps, parallel=False,
                      target_dir=target_dir)



