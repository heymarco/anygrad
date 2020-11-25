from time import process_time


def wait_nonblocking(duration: float):
    start = process_time()
    now = process_time()
    while now - start < duration:
        now = process_time()

