import numpy as np
from scipy.stats import random_correlation
import pandas as pd


def add_random_correlation(data):
    """
    Step 5
    """
    dims = data.shape[-1]
    evs = np.random.uniform(0.01, 1, size=dims)
    evs = evs / np.sum(evs) * dims
    random_corr_matrix = random_correlation.rvs(evs)
    print(pd.DataFrame(random_corr_matrix))
    cholesky_transform = np.linalg.cholesky(random_corr_matrix)
    normal_eq_mean = cholesky_transform.dot(data.T)  # Generating random MVN (0, cov_matrix)
    normal_eq_mean = normal_eq_mean.transpose()
    normal_eq_mean = normal_eq_mean.transpose()  # Transposing back
    data = normal_eq_mean.T
    return data