import time


def get_timestamp(unit: str = "µs") -> float:
    current_time = time.process_time_ns()
    if unit == "ns":
        return float(current_time)
    elif unit == "µs":
        return float(current_time) / 1e3
    elif unit == "ms":
        return float(current_time) / 1e6
    elif unit == "s":
        return float(current_time) / 1e9


def measure_duration(process):
    def measure(*args, **kwargs):
        pre = get_timestamp()
        result = process(*args, *kwargs)
        post = get_timestamp()
        return post - pre, result
    return measure
