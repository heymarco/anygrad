from time import process_time
import copy

import torch
from sklearn.cluster import MiniBatchKMeans
from sklearn.mixture import GaussianMixture
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import silhouette_score
from src.abstract.IterativeAlgorithm import IterativeAlgorithm
from src.pathfinding.node_class import NodeClass
from src.torch_models.convolutional import ConvolutionalAE
import numpy as np
from torch.utils.data import DataLoader


class GaussianMixtureAlg(IterativeAlgorithm):
    def __init__(self, n_clusters: int, covariance_type: str, init_mode: str = "random"):
        super(GaussianMixtureAlg, self).__init__()
        self.n_clusters = n_clusters
        self.init_mode = init_mode
        self.total_iterations: int = 0
        self.alg = GaussianMixture(n_components=n_clusters, warm_start=True,
                                   init_params=init_mode, covariance_type=covariance_type,
                                   tol=1.0E-5, max_iter=1)

    def partial_fit(self, X, num_iterations: int) -> (float, int):
        self.total_iterations += num_iterations
        self.alg.max_iter = num_iterations
        start = process_time()
        self.alg.fit(X)
        return process_time() - start, num_iterations

    def warm_up(self, X) -> float:
        return 0.0

    def validate(self, X) -> float:
        return self.alg.lower_bound_

    def should_terminate(self, *args, **kwargs) -> bool:
        try:
            return process_time() - self.start_time > 20 * 60 or self.alg.converged_
        except AttributeError:
            return process_time() - self.start_time > 20 * 60


class MiniBatchKMeansAlg(IterativeAlgorithm):
    def __init__(self, n_clusters: int, batch_size: int = 100, init_mode: str = "random"):
        super(MiniBatchKMeansAlg, self).__init__()
        self.n_clusters = n_clusters
        self.batch_size = batch_size
        self.init_mode = init_mode
        self.default_score = 0.0
        self.total_iterations: int = 0
        self.alg = MiniBatchKMeans(n_clusters=self.n_clusters, init=self.init_mode,
                                   max_iter=1, batch_size=self.batch_size, max_no_improvement=False,
                                   compute_labels=False)

    def partial_fit(self, X, num_iterations: int) -> (float, int):
        self.total_iterations += num_iterations
        start = process_time()
        [self.alg.partial_fit(X) for _ in range(num_iterations)]
        return (process_time() - start), num_iterations

    def validate(self, X) -> float:
        labels = self.alg.predict(X)
        return silhouette_score(X, labels)

    def warm_up(self, X):
        return self.default_score

    def should_terminate(self, *args, **kwargs) -> bool:
        return process_time() - self.start_time > 2*60


class MLPAlg(IterativeAlgorithm):
    def __init__(self, neurons_hidden, learning_rate: float, batch_size: int = 32):
        super(MLPAlg, self).__init__()
        self.neurons_hidden = neurons_hidden
        self.total_iterations: int = 0
        self.batch_size = batch_size
        self.learning_rate = learning_rate
        self.alg = MLPRegressor(hidden_layer_sizes=neurons_hidden,
                                activation='tanh',
                                solver='sgd',
                                batch_size=batch_size,
                                learning_rate="constant",
                                learning_rate_init=learning_rate)
        self.validation_data = None
        self.train_data = None

    def partial_fit(self, X, num_iterations: int) -> (float, int):
        self.total_iterations += num_iterations
        # data_indices = np.random.choice([i for i in range(len(X))],
        #                                 size=self.batch_size*num_iterations)
        start = process_time()
        for _ in range(num_iterations):
            self.alg.partial_fit(X, X)
        # data = X[data_indices]
        # assert len(data_indices) == len(data)
        # start = process_time()
        # for i in range(num_iterations):
        #     d = data[i*32:(i+1)*32]
        #     self.alg.partial_fit(d, d)
        return (process_time() - start), num_iterations

    def warm_up(self, X) -> float:
        return self.validate(X)

    def validate(self, X) -> float:
        prediction = self.alg.predict(X)
        diff = X - prediction
        norm = np.mean([np.linalg.norm(d) for d in diff])
        return -norm

    def should_terminate(self, *args, **kwargs) -> bool:
        return self.total_iterations >= 100


class ConvolutionalAEAlg(IterativeAlgorithm):

    def __init__(self, num_channels: int, num_filters: int, learning_rate: float,
                 batch_size: int = 32):
        super(ConvolutionalAEAlg, self).__init__()
        self.num_filters = num_filters
        self.total_iterations: int = 0
        self.batch_size = batch_size
        self.learning_rate = learning_rate
        self.alg = ConvolutionalAE(num_channels=num_channels,
                                   num_filters=num_filters,
                                   learning_rate=learning_rate)
        self.validation_data = None
        self.train_data = None
        self.device = self.__get_device__()
        self.alg.to(self.device)

    def partial_fit(self, X, num_iterations: int) -> (float, int):
        self.total_iterations += num_iterations
        assert type(X) is DataLoader
        X: DataLoader = X
        self.alg.train()
        iter_dataloader = iter(X)
        iterations = range(num_iterations)
        start = process_time()
        for _ in iterations:
            batch, _ = next(iter_dataloader)
            batch = batch.to(self.device)
            self.alg.optimizer.zero_grad()
            pred = self.alg.forward(batch)
            self.alg.loss(pred, batch).backward()
            self.alg.optimizer.step()
        return (process_time() - start), num_iterations

    def warm_up(self, X) -> float:
        return self.validate(X)

    def validate(self, X) -> float:
        total_loss = 0.0
        with torch.no_grad():
            self.alg.eval()
            for data, _ in iter(X):
                data = data.to(self.device)
                pred = self.alg.forward(data)
                total_loss -= self.alg.loss(pred, data).item()
        return total_loss

    def should_terminate(self, *args, **kwargs) -> bool:
        return process_time() - self.start_time > 60*60.

    def __get_device__(self):
        if torch.cuda.is_available():
            print("Running on GPU")
            device = 'cuda:0'
        else:
            print("Running on CPU")
            device = 'cpu'
        return device


class MAStarAlg(IterativeAlgorithm):

    def __init__(self):
        super().__init__()
        self.goal = np.array([[1, 2, 3],
                              [4, 5, 6],
                              [7, 8, 9]])
        self.startNode = NodeClass(self.shuffle_board(self.goal, 8000), 0, self.goal)
        self.upper_bound = None
        self.open_list = [self.startNode]
        self.closed_list = []
        self.current_node = None

    def partial_fit(self, X, num_iterations: int) -> (float, int):
        iterations_spent = 0
        self.current_node = self.get_min(self.open_list)
        self.open_list.remove(self.current_node)
        # Add to closed list
        self.closed_list.append(self.current_node)
        start = process_time()
        while iterations_spent < num_iterations:
            iterations = self.expand(self.current_node)
            iterations_spent += iterations
        return process_time() - start, iterations_spent

    def validate(self, X) -> float:
        if len(self.open_list) > 0:
            return min([item.f for item in self.open_list])
        else:
            return 0.0

    def warm_up(self, X) -> float:
        pass

    def should_terminate(self, *args, **kwargs) -> bool:
        pass

    def shuffle_board(self, board, n):
        temp = copy.deepcopy(board)
        for i in range(n):
            # random direction
            direction = np.random.choice(range(4), 1)
            a, b = np.where(temp == board.shape[0] ** 2)
            a = a[0]
            b = b[0]
            # Move blank up
            if direction == 0 and a != 0:
                temp_value = temp[a - 1][b]
                temp[a - 1][b] = temp.shape[0] ** 2
                temp[a][b] = temp_value

            # Move blank down
            if direction == 1 and a != board.shape[0] - 1:
                temp_value = temp[a + 1][b]
                temp[a + 1][b] = board.shape[0] ** 2
                temp[a][b] = temp_value

            # Move blank left
            if direction == 2 and b != 0:
                temp_value = temp[a][b - 1]
                temp[a][b - 1] = board.shape[0] ** 2
                temp[a][b] = temp_value

            # Move blank right
            if direction == 3 and b != board.shape[0] - 1:
                temp_value = temp[a][b + 1]
                temp[a][b + 1] = board.shape[0] ** 2
                temp[a][b] = temp_value

        return temp

    def expand(self, node: NodeClass) -> int:
        # Generate successors
        successors = node.generate_successor()

        # Iterate over children
        for s in successors:
            # Calculate costs and compare with upper bound
            s.calculate_costs()

            # print temp[j]
            # Check if successor is the goal node
            if self.upper_bound is None or s.f < self.upper_bound.f:
                self.iterate(s)
        spent_iterations = len(successors)
        return spent_iterations

    def iterate(self, node: NodeClass):
        if node.is_goal():
            # update upper bound
            upper_bound = node
            print('New Upper Bound:****************************************************')
            print(upper_bound)
            print('')

        # Check for better duplicates in open_list
        elif node in self.open_list:
            k = self.open_list.index(node)
            if node.g >= self.open_list[k].g:
                return
            else:
                self.open_list.remove(node)
                self.open_list.append(node)

        # Check for better duplicates in closed_list
        elif node in self.closed_list:
            k = self.closed_list.index(node)
            if node.g >= self.closed_list[k].g:
                return
            else:
                self.closed_list.remove(node)
                self.open_list.append(node)
        else:
            self.open_list.append(node)

    def get_min(self, temp):
        min_index = np.argmin([item.fp for item in temp])
        min_index2 = min_index
        for i in range(len(temp)):
            if temp[i].fp == temp[min_index].fp:
                if temp[i].h < temp[min_index2].h:
                    min_index2 = i
        return temp[min_index2]
