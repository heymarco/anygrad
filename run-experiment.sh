
# parse arguments
while getopts e:f:t:p:r: option
do
case "${option}"
in
f) FILE=${OPTARG};;
e) EXPERIMENT=${OPTARG};;
t) TARGET=${OPTARG};;
p) PARALLEL=${OPTARG};;
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
	echo "Error, please provide the experiment via '-e exp_id'! Possible options are 't-over-m' and 'anygrad'"
	echo ""
	exit -1
fi

if [[ ! "$PARALLEL" ]]; then
	echo "Warning, parallelism level not specified. Not running parallel (default)."
	echo "You can specify the parallelism level via '-p'."
	echo ""
	PARALLEL=0
fi

if [[ ! "$REPETITIONS" ]]; then
	echo "Warning, no number of repetitions specified, performing 1 repetition."
	echo "You can specify the number of repetitions using '-r'."
	echo ""
	REPETITIONS=1
fi

TARGET_DIR="$WORKING_DIR/$TARGET/$(timestamp)"
TARGET_DIR=$(clean_path $TARGET_DIR)

# create target dir if needed
if [ ! -d "$TARGET_DIR" ]; then
	echo "Creating target directory at $TARGET_DIR"
  	mkdir $TARGET_DIR
fi

touch "$(clean_path $TARGET_DIR/args.txt)"
echo "Experiment: $EXPERIMENT" > "$(clean_path $TARGET_DIR/args.txt)"
echo "Repetitions: $REPETITIONS" >> "$(clean_path $TARGET_DIR/args.txt)"

java -jar target/scala-2.13/anygrad-assembly-0.0.1.jar -t $TARGET_DIR -e $EXPERIMENT -f $FILE -p $PARALLEL -r $REPETITIONS

echo "Done with execution"

