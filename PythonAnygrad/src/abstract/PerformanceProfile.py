from abc import ABC, abstractmethod
import numpy as np


class PerformanceProfile(ABC):

    @abstractmethod
    def value(self, M: int) -> float:
        pass

    @abstractmethod
    def first_derivation(self, M: int) -> float:
        pass

    @abstractmethod
    def second_derivation(self, M: int) -> float:
        pass


class PowerCurve(PerformanceProfile):

    def __init__(self, a, b):
        self.a = a
        self.b = b

    def value(self, M: int) -> float:
        return self.a * M ** self.b

    def first_derivation(self, M: int) -> float:
        return self.a * (M ** (self.b - 1) * self.b)

    def second_derivation(self, M: int) -> float:
        return self.a * (-1 + self.b) * self.b * M ** (self.b - 2)


class AsymptoticRegressionModel(PerformanceProfile):

    def __init__(self, a, b, c):
        self.a = a
        self.b = b
        self.c = c

    def value(self, M: int) -> float:
        return self.a - (self.a - self.b) * np.exp(-self.c * M)

    def first_derivation(self, M: int) -> float:
        return (self.a - self.b) * (np.exp(-self.c * M) * self.c)

    def second_derivation(self, M: int) -> float:
        return self.c**2 * (-(self.a - self.b)) * np.exp(-self.c * M)
