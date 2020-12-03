import argparse
import os
import json
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

from src.features import *
from src.utils import load_all_json_in_dir
from src.plotting import prepare_df, plot_df


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-dir", type=str, help="The path to the result directory")
    args = parser.parse_args()
    data = load_all_json_in_dir(args.dir)
    df = prepare_df(data)

    x_list = ["duration", "iterations"]
    y_list = ["result", "utility", "quality", "m"]

    for x in x_list:
        for y in y_list:
            plot_df(df, x, y, fill_between=False)
            plt.savefig(os.path.join(args.dir, "{}_{}.pdf".format(x, y)))
            plt.clf()
