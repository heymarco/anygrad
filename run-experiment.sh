
# parse arguments
while getopts f:s:m:t:p:r:e:E:q:S: OPTION;
do
echo $OPTION
case $OPTION
in
f)            FILE=${OPTARG};;
E)            EXPERIMENT=${OPTARG};;
t)            TARGET=${OPTARG};;
p)            PARALLEL=${OPTARG};;
r)            REPETITIONS=${OPTARG};;
s)            SLEEP=${OPTARG};;
m)		        ITERATIONS=${OPTARG};;
e)		        EPS=${OPTARG};;
q)            QUALITY=${OPTARG};;
S)            STRATEGY=${OPTARG};;
\?) echo "Invalid option -$OPTARG" >&2;;
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
	echo "Error, please provide the experiment via '-E exp_id'! Possible options are 't-over-m', 'singlerun' and 'comparison'"
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

if [[ ! "$SLEEP" ]]; then
	echo "Warning, additional switching time not specified, using default of 0.0."
	echo "You can add additional sleeping time using '-s'."
	echo ""
	SLEEP="0.0"
fi

if [[ ! "$EPS" ]]; then
	echo "Warning, epsilon not specified, using default of 0.03."
	echo "You can specify epsilon using '-e'."
	echo ""
	EPS=0.03
fi

if [[ ! "$QUALITY" ]]; then
	echo "Warning, quality not specified, using default of 0.9."
	echo "You can specify quality using '-q'."
	echo ""
	QUALITY=0.9
fi

if [[ ! "$ITERATIONS" ]]; then
	echo "Warning, iterations not specified, using default of 5."
	echo "You can specify iterations using '-m'."
	echo ""
	ITERATIONS=5
fi

if [[ ! "$STRATEGY" ]]; then
	echo "Warning, strategy not specified, using baseline as default."
	echo "You can specify a strategy using '-S'. Options are baseline, anygrad, anygrad_sa, and anygrad_sp"
	echo ""
	STRATEGY="baseline"
fi


TARGET_DIR="$WORKING_DIR/$TARGET/"
TARGET_DIR=$(clean_path $TARGET_DIR)

# create target dir if needed
if [ ! -d "$TARGET_DIR" ]; then
	echo "Creating target directory at $TARGET_DIR"
  	mkdir $TARGET_DIR
fi

# touch "$(clean_path $TARGET_DIR/args.txt)"
# echo "Experiment: $EXPERIMENT" > "$(clean_path $TARGET_DIR/args.txt)"
# echo "Repetitions: $REPETITIONS" >> "$(clean_path $TARGET_DIR/args.txt)"

java -Xmx20480m -jar target/scala-2.12/anygrad-assembly-0.0.1.jar -t $TARGET_DIR -e $EXPERIMENT -f $FILE -p $PARALLEL -r $REPETITIONS -s $SLEEP -eps $EPS -m $ITERATIONS -q $QUALITY -strategy $STRATEGY
