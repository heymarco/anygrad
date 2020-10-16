import os
import json
import numpy as np


def parse_array(from_json):
    num_reps = len(from_json)
    num_rounds = len(from_json[0])
    num_rows = len(from_json[0][0])
    num_cols = len(from_json[0][0][0])
    num_metrics = len(from_json[0][0][0][0])
    array_shape = (num_reps, num_rounds, num_rows, num_cols, num_metrics)
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
