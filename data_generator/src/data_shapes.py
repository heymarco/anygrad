import numpy as np
import pandas as pd


def normal_data(n_dims, n_data):
    return np.random.normal(size=(n_data, n_dims))


def uniform_data(n_dims, n_data):
    return np.random.normal(scale=(n_data, n_dims))