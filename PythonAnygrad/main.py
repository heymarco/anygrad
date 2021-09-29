import os
from argparse import ArgumentParser
from src.experiment import create_kmeans_experiment, create_mlp_experiment, create_cifar_experiment, \
    create_gmm_experiment, create_baseline_comparison_cifar, create_baseline_comparison_gmm, create_a_star_experiment
import numpy as np

from sklearn.datasets import load_digits, fetch_openml


if __name__ == '__main__':

    parser = ArgumentParser()
    parser.add_argument("-data_dir", type=str, required=False)
    parser.add_argument("-reps", type=int, required=True)
    parser.add_argument("-E", type=str, required=True, choices=("gmm",
                                                                "mlp",
                                                                "kmeans_batch",
                                                                "cifar",
                                                                "baselines_conv",
                                                                "baselines_gmm",
                                                                "a_star"))
    parser.add_argument("-N", type=int, required=True, help="The number of targets")
    parser.add_argument("-sleep", type=float, default=0.0)
    args = parser.parse_args()

    num_targets = args.N
    target_dir = ""
    if args.E == "gmm":
        target_dir = "gmm_anygrad"
        experiment = create_gmm_experiment
    elif args.E == "kmeans_batch":
        experiment = create_kmeans_experiment
    elif args.E == "mlp":
        experiment = create_mlp_experiment
    elif args.E == "cifar":
        target_dir = "conv_anygrad"
        experiment = create_cifar_experiment
    elif args.E == "baselines_conv":
        experiment = create_baseline_comparison_cifar
        target_dir = "conv_baselines"
    elif args.E == "baselines_gmm":
        target_dir = "gmm_baselines"
        experiment = create_baseline_comparison_gmm
    elif args.E == "a_star":
        target_dir = "a_star"
        experiment = create_a_star_experiment

    experiment = experiment(num_targets=num_targets,
                            num_reps=args.reps,
                            target_dir=os.path.join("save", target_dir),
                            sleep=args.sleep)


    experiment.run()
