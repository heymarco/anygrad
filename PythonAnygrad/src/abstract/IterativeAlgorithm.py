from abc import ABC, abstractmethod
from time import process_time
import numpy as np


class IterativeAlgorithm(ABC):

    def __init__(self):
        self.batch_size = None
        self.start_time = None
        self.default_score = np.nan

    @abstractmethod
    def partial_fit(self, X, num_iterations: int):
        pass

    @abstractmethod
    def validate(self, X):
        pass

    @abstractmethod
    def warm_up(self, X) -> float:
        return self.default_score

    @abstractmethod
    def should_terminate(self, *args, **kwargs) -> bool:
        pass

    def set_start(self):
        self.start_time = process_time()

