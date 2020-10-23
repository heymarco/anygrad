import os
import json
import numpy as np
import pandas as pd


def parse_array(from_json):
    max_num_rounds = 0
    max_num_rows = 0
    max_num_cols = 0
    num_metrics = len(from_json[0][0][0][0])
    # find maximum required shape
    for rep in range(len(from_json)):
        max_num_rounds = max_num_rounds if len(from_json[rep]) < max_num_rounds else len(from_json[rep])
        for round in range(len(from_json[rep])):
            max_num_rows = max_num_rows if len(from_json[rep][round]) < max_num_rows else len(from_json[rep][round])
            for row in range(len(from_json[rep][round])):
                max_num_cols = max_num_cols if len(from_json[rep][round][row]) < max_num_cols else len(from_json[rep][round][row])
    array_shape = (len(from_json), max_num_rounds, max_num_rows, max_num_cols, num_metrics)
    data_array = np.full(shape=array_shape, fill_value=np.nan)
    for i in range(len(from_json)):
        for j in range(len(from_json[i])):
            for k in range(len(from_json[i][j])):
                for l in range(len((from_json[i][j][k]))):
                    for m, key in enumerate(sorted(from_json[i][j][k][l].keys())):
                        if from_json[i][j][k][l][key] is not None:
                            data_array[i, j, k, l, m] = float(from_json[i][j][k][l][key])
                        else:
                            data_array[i, j, k, l, m] = np.nan
    return data_array


def load_all_in_dir(directory):
    (_, _, filenames) = next(os.walk(directory))
    filenames = [fn for fn in filenames if fn.endswith(".json")]
    files = {}
    for fn in filenames:
        filepath = os.path.join(directory, fn)
        j = json.load(open(filepath))
        files[fn] = parse_array(j)
    return files


def aggregate_rounds(df):
    oldshape = df.shape
    newshape = [oldshape[0]*oldshape[1]]
    newshape = newshape + [oldshape[i] for i in range(2, len(oldshape))]
    df = np.reshape(df, newshape=newshape)
    return df


def running_mean(arr, window_size):
    series = pd.Series(arr)
    return series.rolling(window=window_size).mean().to_numpy()
    # return np.convolve(arr, np.ones((window_size,)) / window_size, mode='valid')


def running_std(arr, window_size):
    series = pd.Series(arr)
    return series.rolling(window=window_size).std().to_numpy()
