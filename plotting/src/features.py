import numpy as np


keys = ['M', 'duration', 'iterations', 'm', 'quality', 'result', 't1', 'tcs', 'utility', 'variance']


def result(d):
    return __get_at_(d, keys.index("result"))


def variance(d):
    return __get_at_(d, keys.index("variance"))


def quality(d, below_zero=False):
    d = __get_at_(d, keys.index("quality"))
    return d if below_zero else np.maximum(d, np.zeros(d.shape))


def utility(d):
    return __get_at_(d, keys.index("utility"))


def m(d):
    return __get_at_(d, keys.index("m"))


def M(d):
    return __get_at_(d, keys.index("M"))


def iterations(d):
    return __get_at_(d, keys.index("iterations"))


def duration(d):
    return __get_at_(d, keys.index("duration"))


def tcs(d):
    return __get_at_(d, keys.index("tcs"))


def t1(d):
    return __get_at_(d, keys.index("t1"))


def __get_at_(d, position):
    return d[:, :, :, :, position]
