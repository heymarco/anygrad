from argparse import ArgumentParser
from src.experiment import create_kmeans_experiment, create_mlp_experiment, create_cifar_experiment, \
    create_gmm_experiment, create_baseline_comparison_cifar, create_baseline_comparison_gmm
import numpy as np

from sklearn.datasets import load_digits, fetch_openml


if __name__ == '__main__':

    parser = ArgumentParser()
    parser.add_argument("-data_dir", type=str, required=True)
    parser.add_argument("-reps", type=int, required=True)
    parser.add_argument("-E", type=str, required=True, choices=("gmm",
                                                                "mlp",
                                                                "kmeans_batch",
                                                                "cifar",
                                                                "baselines_conv",
                                                                "baselines_gmm"))
    parser.add_argument("-N", type=int, required=True, help="The number of targets")
    parser.add_argument("-sleep", type=float, default=0.0)
    args = parser.parse_args()

    num_targets = args.N
    if args.E == "gmm":
        experiment = create_gmm_experiment
    elif args.E == "kmeans_batch":
        experiment = create_kmeans_experiment
    elif args.E == "mlp":
        experiment = create_mlp_experiment
    elif args.E == "cifar":
        experiment = create_cifar_experiment
    elif args.E == "baselines_conv":
        experiment = create_baseline_comparison_cifar
    elif args.E == "baselines_gmm":
        experiment = create_baseline_comparison_gmm

    experiment = experiment(num_targets=num_targets,
                            num_reps=args.reps,
                            target_dir="save",
                            sleep=args.sleep)


    experiment.run()
