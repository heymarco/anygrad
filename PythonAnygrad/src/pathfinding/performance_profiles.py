import numpy as np
from src.abstract.PerformanceProfile import AsymptoticRegressionModel


class EightPuzzlePerformanceProfile(AsymptoticRegressionModel):

    def __init__(self):
        """
        Parameterization obtained with scipy curve fitting
        The fitted curve is min-max-scaled
        """
        a = 1.0
        b = 0.0
        c = 0.0012774981510894291
        super().__init__(a, b, c)
