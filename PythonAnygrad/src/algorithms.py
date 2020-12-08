from time import process_time

import torch
from sklearn.cluster import MiniBatchKMeans
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import silhouette_score
from src.abstract.IterativeAlgorithm import IterativeAlgorithm
from src.torch_models.convolutional import ConvolutionalAE
import numpy as np
from torch.utils.data import DataLoader


class MiniBatchKMeansAlg(IterativeAlgorithm):
    def __init__(self, n_clusters: int, batch_size: int = 100, init_mode: str = "random"):
        super(MiniBatchKMeansAlg, self).__init__()
        self.n_clusters = n_clusters
        self.batch_size = batch_size
        self.init_mode = init_mode
        self.total_iterations: int = 0
        self.alg = MiniBatchKMeans(n_clusters=self.n_clusters, init=self.init_mode,
                                   max_iter=1, batch_size=self.batch_size, max_no_improvement=False,
                                   compute_labels=False)

    def partial_fit(self, X, num_iterations: int):
        self.total_iterations += num_iterations
        start = process_time()
        [self.alg.partial_fit(X) for _ in range(num_iterations)]
        return process_time() - start

    def validate(self, X):
        labels = self.alg.predict(X)
        return silhouette_score(X, labels)

    def warm_up(self, X):
        self.alg.partial_fit(X[self.n_clusters])

    def should_terminate(self, *args, **kwargs) -> bool:
        return self.total_iterations > 60


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

    def partial_fit(self, X, num_iterations: int):
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
        duration = process_time() - start
        return duration

    def warm_up(self, X):
        self.alg.partial_fit(X[:self.batch_size], X[:self.batch_size])

    def validate(self, X):
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

    def partial_fit(self, X, num_iterations: int):
        self.total_iterations += num_iterations
        assert type(X) is DataLoader
        start = process_time()
        self.alg.train()
        for i in range(num_iterations):
            for batch in X:
                batch = batch.to(self.device)
                self.alg.optimizer.zero_grad()
                pred = self.alg.forward(batch)
                self.alg.loss(pred, batch).backward()
                self.alg.optimizer.step()
        return process_time() - start

    def warm_up(self, X):
        pass

    def validate(self, X):
        total_loss = 0.0
        with torch.no_grad():
            self.alg.eval()
            for data, _ in iter(X):
                data = data.to(self.device)
                pred = self.alg.forward(data)
                total_loss -= self.alg.loss(pred, data).item()
        return total_loss

    def should_terminate(self, *args, **kwargs) -> bool:
        return process_time() - self.start_time > 60*3.

    def __get_device__(self):
        if torch.cuda.is_available():
            print("Running on GPU")
            device = 'cuda:0'
        else:
            print("Running on CPU")
            device = 'cpu'
        return device
