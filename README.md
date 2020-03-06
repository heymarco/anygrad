# AnyGrad
## Approximation of large dependency matrices in an 'anytime' fashion using gradient ascent

### Execute

The easy way:

    sbt assembly
    bash run-experiment.sh -t target-dir -f filepath -p parallel [0, 1] -e experiment [t-over-m] -r repetitions [Int]
    
- `t`: Where to store the results. Results are exported as `json` and stored in a `.txt` file.
- `f`: Path to the data (csv-file, delimiter=",")
- `p`: Use parallelization
- `e`: Select the desired experiment. Possible options are `t-over-m`
- `r`: Specify the number of repetitions of the experiment

