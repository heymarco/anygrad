from time import process_time

import numpy as np
import torch


def wait_nonblocking(duration: float):
    start = process_time()
    now = process_time()
    while now - start < duration:
        now = process_time()


def set_random_state(rs: int):
    torch.manual_seed(rs)
    np.random.seed(rs)