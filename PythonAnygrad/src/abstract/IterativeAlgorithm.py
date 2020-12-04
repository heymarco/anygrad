from abc import ABC, abstractmethod
from time import process_time


class IterativeAlgorithm(ABC):

    def __init__(self):
        self.batch_size = None
        self.start_time = process_time()

    @abstractmethod
    def partial_fit(self, X, num_iterations: int):
        pass

    @abstractmethod
    def validate(self, X):
        pass

    @abstractmethod
    def should_terminate(self, *args, **kwargs) -> bool:
        pass

