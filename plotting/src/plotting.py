import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd

from src.features import *
from src.utils import aggregate_rounds, running_mean, running_std


def prepare_df(data_dict):
    df = []
    for key in data_dict:
        data = data_dict[key]
        num_reps = len(data)
        _result = np.nanmean(result(data), axis=(-1, -2)).flatten()
        _utility = np.nanmean(quality(data) * utility(data), axis=(-1, -2)).flatten()
        _quality = np.nanmean(quality(data), axis=(-1, -2)).flatten()
        _iterations = np.nanmax(iterations(data), axis=(-1, -2)).flatten()
        _duration = np.nanmax(duration(data), axis=(-1, -2)).flatten()
        _variance = np.nanmean(variance(data), axis=(-1, -2)).flatten()
        _m = np.nanmean(m(data), axis=(-1, -2)).flatten()
        _M = np.nanmax(M(data), axis=(-1, -2)).flatten()
        _tcs = np.nanmean(tcs(data), axis=(-1, -2)).flatten()
        _t1 = np.nanmean(t1(data), axis=(-1, -2)).flatten()

        print(running_std(_utility, window_size=num_reps))

        sorted_indices = np.argsort(_duration)
        duration_mean = running_mean(_duration[sorted_indices], window_size=num_reps)
        result_mean = running_mean(_result[sorted_indices], window_size=num_reps)
        utility_mean = running_mean(_utility[sorted_indices], window_size=num_reps)
        quality_mean = running_mean(_quality[sorted_indices], window_size=num_reps)
        iterations_mean = running_mean(_iterations[sorted_indices], window_size=num_reps)
        variance_mean = running_mean(_variance[sorted_indices], window_size=num_reps)
        m_mean = running_mean(_m[sorted_indices], window_size=num_reps)
        M_mean = running_mean(_M[sorted_indices], window_size=num_reps)
        tcs_mean = running_mean(_tcs[sorted_indices], window_size=num_reps)
        t1_mean = running_mean(_t1[sorted_indices], window_size=num_reps)

        sorted_indices = np.argsort(_duration)
        duration_std = running_std(_duration[sorted_indices], window_size=num_reps)
        result_std = running_std(_result[sorted_indices], window_size=num_reps)
        utility_std = running_std(_utility[sorted_indices], window_size=num_reps)
        quality_std = running_std(_quality[sorted_indices], window_size=num_reps)
        iterations_std = running_std(_iterations[sorted_indices], window_size=num_reps)
        variance_std = running_std(_variance[sorted_indices], window_size=num_reps)
        m_std = running_std(_m[sorted_indices], window_size=num_reps)
        M_std = running_std(_M[sorted_indices], window_size=num_reps)
        tcs_std = running_std(_tcs[sorted_indices], window_size=num_reps)
        t1_std = running_std(_t1[sorted_indices], window_size=num_reps)

        tcs_t1_std = running_std(_tcs[sorted_indices] / _t1[sorted_indices], window_size=num_reps)
        t1_tcs_std = running_std(_t1[sorted_indices] / _tcs[sorted_indices], window_size=num_reps)

        for i in range(len(duration_mean)):
            df.append([
                result_mean[i],
                utility_mean[i],
                quality_mean[i],
                iterations_mean[i],
                duration_mean[i],
                variance_mean[i],
                m_mean[i],
                M_mean[i],
                tcs_mean[i],
                t1_mean[i],
                t1_mean[i] / tcs_mean[i],
                tcs_mean[i] / t1_mean[i],
                result_std[i],
                utility_std[i],
                quality_std[i],
                iterations_std[i],
                duration_std[i],
                variance_std[i],
                m_std[i],
                M_std[i],
                tcs_std[i],
                t1_std[i],
                t1_tcs_std[i],
                tcs_t1_std[i],
                key[:-5]
            ])
    df = pd.DataFrame(df, columns=["result",
                                   "utility",
                                   "quality",
                                   "iterations",
                                   "duration",
                                   "variance",
                                   "m",
                                   "M",
                                   "tcs",
                                   "t1",
                                   "t1/tcs",
                                   "tcs/t1",
                                   "result_std",
                                   "utility_std",
                                   "quality_std",
                                   "iterations_std",
                                   "duration_std",
                                   "variance_std",
                                   "m_std",
                                   "M_std",
                                   "tcs_std",
                                   "t1_std",
                                   "t1/tcs_std",
                                   "tcs/t1_std",
                                   "Strategy"
                                   ])
    return df


def plot_df(df, x, y, add_x="", add_y="", window_size=100, fill_between=True):
    sns.lineplot(x=x, y=y, data=df, hue="Strategy", style="Strategy")
    all_lines = plt.gca().get_lines()
    if not fill_between:
        return
    for strategy in np.unique(df["Strategy"]):
        this_line = [line for line in all_lines if line.get_label() == strategy][0]
        this_color = this_line.get_color()
        this_x = df[df["Strategy"] == strategy][x]
        this_mean = df[df["Strategy"] == strategy][y]
        this_std = df[df["Strategy"] == strategy][y + "_std"]
        plt.fill_between(this_x, this_mean-this_std, this_mean+this_std, color=this_color, alpha=0.3)
