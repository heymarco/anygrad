import argparse
import json
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

from src.features import *
from src.utils import load_all_in_dir
from src.plotting import prepare_df, plot_df


if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument("-dir", type=str, help="The path to the result directory")
    args = parser.parse_args()
    data = load_all_in_dir(args.dir)
    df = prepare_df(data)
    plot_df(df, "duration", "utility")
    plt.show()

