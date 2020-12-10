from argparse import ArgumentParser
from src.experiment import create_kmeans_experiment, create_mlp_experiment, create_cifar_experiment, \
    create_gmm_experiment
import numpy as np

from sklearn.datasets import load_digits, fetch_openml


if __name__ == '__main__':

    parser = ArgumentParser()
    parser.add_argument("-data_dir", type=str, required=True)
    parser.add_argument("-reps", type=int, required=True)
    parser.add_argument("-E", type=str, required=True, choices=("gmm", "mlp", "kmeans_batch", "cifar"))
    parser.add_argument("-N", type=int, required=True, help="The number of targets")
    parser.add_argument("-sleep", type=float, default=0.0)
    args = parser.parse_args()

    # X_digits, y_digits = load_digits(return_X_y=True)
    X, y = fetch_openml(name="cardiotocography", version=2, return_X_y=True)

    n_digits = len(np.unique(y))
    labels = y

    num_targets = args.N
    if args.E == "gmm":
        experiment = create_gmm_experiment(num_targets=num_targets,
                                           num_reps=args.reps,
                                           target_dir="save",
                                           sleep=args.sleep)
    if args.E == "kmeans_batch":
        experiment = create_kmeans_experiment(num_targets=num_targets,
                                              num_reps=args.reps,
                                              target_dir="save",
                                              sleep=args.sleep)
    elif args.E == "mlp":
        experiment = create_mlp_experiment(num_targets=num_targets,
                                           num_reps=args.reps,
                                           target_dir="save",
                                           sleep=args.sleep)
    elif args.E == "cifar":
        experiment = create_cifar_experiment(num_targets=num_targets,
                                             num_reps=args.reps,
                                             target_dir="save",
                                             sleep=args.sleep)

    experiment.run()
