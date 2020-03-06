
# parse arguments
while getopts e:m:f:t:p:w:r: option
do
case "${option}"
in
f) FILE=${OPTARG};;
m) ITERATIONS=${OPTARG};;
e) EXPERIMENT=${OPTARG};;
t) TARGET=${OPTARG};;
p) PARALLEL=${OPTARG};;
w) WEIGHTS=${OPTARG};;
r) REPETITIONS=${OPTARG};;

esac
done

timestamp() {
	date +%s
}

clean_path() {
	pattern="//"
	path=$1
	CLEANED=${path//${pattern}//}
	echo $CLEANED
}

# create abs path to working dir
WORKING_DIR=$(pwd)/

if [[ ! "$TARGET" ]]; then
	echo "Error, please provide the relative path to the target directory via '-t'!"
	echo ""
	exit -1
fi

if [[ ! "$FILE" ]]; then
	echo "Error, please provide the relative path to the data directory via '-f'!"
	echo ""
	exit -1
fi

if [[ ! "$EXPERIMENT" ]]; then
	echo "Error, please provide the experiment via '-e exp_id'!"
	echo ""
	exit -1
fi

if [[ ! "$PARALLEL" ]]; then
	echo "Warning, parallelism level not specified. Not running parallel (default)."
	echo "You can specify the parallelism level via '-p'."
	echo ""
	PARALLEL=0
fi

if [[ ! "$ITERATIONS" ]]; then
	echo "Warning, number of iterations not specified. Using default ('-m 1')."
	echo "You can specify the number of iterations per round via '-m'."
	echo "This parameter is only considered in the fixed allocation strategies."
	echo ""
	ITERATIONS=1
fi

if [[ ! "$REPETITIONS" ]]; then
	echo "Warning, no number of repetitions specified, performing 1 repetition."
	echo "You can specify the number of repetitions using '-r'."
	echo ""
	REPETITIONS=1
fi

if [[ ! "$WEIGHTS" ]]; then
	echo "Warning, usage of weights not specified. Using no weights per default."
	echo "You can apply random weights by setting the '-w' flag."
	WEIGHTS=0
fi

TARGET_DIR="$WORKING_DIR/$TARGET/$(timestamp)"
TARGET_DIR=$(clean_path $TARGET_DIR)

DATA_DIR="${TARGET_DIR}/data"
DATA_DIR=$(clean_path $DATA_DIR)

PLOTS_DIR="${TARGET_DIR}/plots"
PLOTS_DIR=$(clean_path $PLOTS_DIR)

# create target dir if needed
if [ ! -d "$TARGET_DIR" ]; then
	echo "Creating target directory at $TARGET_DIR"
  	mkdir $TARGET_DIR
fi
if [ ! -d "$PLOTS_DIR" ]; then
	echo "Creating plots directory at $PLOTS_DIR"
  	mkdir $PLOTS_DIR
  	touch "$(clean_path $PLOTS_DIR/args.txt)"
fi
if [ ! -d "$DATA_DIR" ]; then
	echo "Creating data directory at $DATA_DIR"
  	mkdir $DATA_DIR
fi

echo "Experiment: $EXPERIMENT" > "$(clean_path $PLOTS_DIR/args.txt)"
echo "Repetitions: $REPETITIONS" >> "$(clean_path $PLOTS_DIR/args.txt)"
echo "Weights: $WEIGHTS" >> "$(clean_path $PLOTS_DIR/args.txt)"

java -jar target/scala-2.13/anygrad-assembly-0.0.1.jar -t $TARGET_DIR -e $EXPERIMENT -u vt -m $ITERATIONS -f $FILE -p $PARALLEL -w $WEIGHTS -r $REPETITIONS

echo "Done with execution"

exit

SYSTEM=$(uname -s)
if [[ "$SYSTEM" == "Darwin" ]]; then
	cd ../python
	pipenv run python ../python/plot_confidence.py $TARGET_DIR $REPETITIONS 0 11
	pipenv run python ../python/plot_utility.py $TARGET_DIR $REPETITIONS 0 11
	pipenv run python ../python/plot_solutions.py $TARGET_DIR
	cd $WORKING_DIR
fi
