from argparse import ArgumentParser
from src.Experiment import experiment_factory
import numpy as np

from sklearn.datasets import load_digits, fetch_openml
from sklearn.preprocessing import scale, MinMaxScaler


if __name__ == '__main__':

    parser = ArgumentParser()
    parser.add_argument("-data_dir", type=str, required=True)
    parser.add_argument("-reps", type=int, required=True)
    parser.add_argument("-E", type=str, required=True, choices=("mlp", "kmeans_batch"))
    parser.add_argument("-N", type=int, required=True, help="The number of targets")
    parser.add_argument("-sleep", type=float, default=0.0)
    args = parser.parse_args()

    # X_digits, y_digits = load_digits(return_X_y=True)
    X, y = fetch_openml(name="cardiotocography", version=2, return_X_y=True)

    n_digits = len(np.unique(y))
    labels = y

    num_targets = args.N

    experiment = experiment_factory(args.E,
                                    data=[X],
                                    targets=[i for i in range(num_targets)],
                                    num_reps=args.reps,
                                    file_dir="", target_dir="save",
                                    sleep=args.sleep,
                                    parallel=False)
    experiment.run()
