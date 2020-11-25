from time import process_time
from sklearn.cluster import MiniBatchKMeans
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import silhouette_score
from src.abstract.IterativeAlgorithm import IterativeAlgorithm
import numpy as np


class MiniBatchKMeansAlg(IterativeAlgorithm):
    def __init__(self, n_clusters: int, seed: int, batch_size: int = 100, init_mode: str = "k-means++"):
        self.n_clusters = n_clusters
        self.batch_size = batch_size
        self.init_mode = init_mode
        self.total_iterations: int = 0
        self.seed = seed
        self.alg = MiniBatchKMeans(n_clusters=self.n_clusters, init=self.init_mode,
                                   max_iter=1, batch_size=self.batch_size, max_no_improvement=False,
                                   random_state=seed, compute_labels=False)

    def partial_fit(self, X, num_iterations: int):
        self.total_iterations += num_iterations
        start = process_time()
        [self.alg.partial_fit(X) for _ in range(num_iterations)]
        return process_time() - start

    def validate(self, X):
        return self.alg.score(X)
        # labels = self.alg.predict(X)
        # silhouette = silhouette_score(X, labels)
        # return silhouette

    def should_terminate(self, *args, **kwargs) -> bool:
        return self.total_iterations > 200


class MLPAlg(IterativeAlgorithm):
    def __init__(self, neurons_hidden: int, learning_rate: float, batch_size: int = 32, random_state: int = 0):
        self.neurons_hidden = neurons_hidden
        self.total_iterations: int = 0
        self.batch_size = batch_size
        self.learning_rate = learning_rate
        self.random_state = random_state
        self.alg = MLPRegressor(hidden_layer_sizes=(neurons_hidden,),
                                activation='tanh',
                                solver='sgd',
                                batch_size=batch_size,
                                learning_rate="constant",
                                random_state=random_state,
                                learning_rate_init=learning_rate)
        self.validation_data = None
        self.train_data = None

    def partial_fit(self, X, num_iterations: int):
        self.total_iterations += num_iterations
        data_indices = np.random.choice([i for i in range(len(X))], size=self.batch_size*num_iterations)
        data = X[data_indices]
        start = process_time()
        self.alg.partial_fit(data, data)
        return process_time() - start

    def validate(self, X):
        prediction = self.alg.predict(X)
        diff = X - prediction
        return -np.mean([np.linalg.norm(d) for d in diff])

    def should_terminate(self, *args, **kwargs) -> bool:
        return self.total_iterations > 1000
