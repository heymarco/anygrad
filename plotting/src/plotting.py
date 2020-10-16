import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd

from src.features import *


def prepare_df(data_dict):
    df = []
    for key in data_dict:
        data = data_dict[key]
        _utility = np.nanmean(quality(data) * utility(data), axis=(-1, -2))
        _quality = np.nanmean(quality(data), axis=(-1, -2))
        _iterations = np.nanmax(iterations(data), axis=(-1, -2))
        _duration = np.nanmax(duration(data), axis=(-1, -2))
        _variance = np.nanmean(variance(data), axis=(-1, -2))
        _m = np.nanmean(m(data), axis=(-1, -2))
        _M = np.nanmax(M(data), axis=(-1, -2))
        _tcs = np.nanmean(tcs(data), axis=(-1, -2))
        _t1 = np.nanmean(t1(data), axis=(-1, -2))

        for i in range(len(_utility)):
            for j in range(len(_utility[i])):
                df.append([
                    _utility[i][j],
                    _quality[i][j],
                    _iterations[i][j],
                    _duration[i][j],
                    _variance[i][j],
                    _m[i][j],
                    _M[i][j],
                    _tcs[i][j],
                    _t1[i][j],
                    key[:-5]
                ])
    df = pd.DataFrame(df, columns=["utility",
                                   "quality",
                                   "iterations",
                                   "duration",
                                   "variance",
                                   "m",
                                   "M",
                                   "tcs",
                                   "t1",
                                   "Strategy"
                                   ])
    return df


def plot_df(df, x, y, add_x="", add_y=""):
    sns.lineplot(x=x, y=y, data=df, hue="Strategy", style="Strategy")
