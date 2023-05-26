#!/usr/bin/env bash
. ./config.sh
check
DIR=`pwd`
INST_NAME=${DIR##*$FLOW_INSTALL_DIR/}
CFGDIR="$FLOW_CFG_DIR/$INST_NAME"

DEBUG_FLAGS=""
Log4jOutputPath="$FLOW_LOG_DIR/$INST_NAME"
PIDDIR="$FLOW_STATUS_DIR/$INST_NAME"
PID_FILE="$PIDDIR/registry.pid"
NOHUP_FILE="${Log4jOutputPath}/nohup.out"
JVM_SETTINGS=""
flowregistry_yml="flowregistry.yml"
APP_SETTINGS="-Dspring.config.additional-location=$CFGDIR/cfg/$flowregistry_yml"
REGISTRY_JAR="bin/FlowRegistry*.jar"
LOG4J_CONFIG="-DLog4jOutputPath=$Log4jOutputPath -Dlog4j.configurationFile=${DIR}/cfg/log4j2.xml"
CLASSPATH=".:${DIR}:${DIR}/bin/*:${DIR}/bin:${DIR}/lib/*:${DIR}/cfg/*"

cd $DIR

if [ 0"$CFGDIR" = "0" ]; then
    echo "There is no configuration directory, please initialize."
    exit 1
fi

if [ ! -f "$CFGDIR/cfg/$flowregistry_yml" ];then
  echo "Please initialize registry."
  exit 1
fi

if [ ! -d "$PIDDIR" ]; then
  mkdir -p "$PIDDIR"
fi

if [ ! -d "$Log4jOutputPath" ]; then
  mkdir -p "$Log4jOutputPath"
fi

function checkIfIsRunning() {

  if [ ! -e "${PID_FILE}" ]
  then
    return
  fi

  # Pid file exists, but is the process running?
  OLD_PID=$(cat "${PID_FILE}")
  ps -p "${OLD_PID}" > /dev/null 2>&1
  IS_RUNNING=$?

  if [ "${IS_RUNNING}" -eq 0 ]
  then
    # print to stderr
    >&2 echo "This FlowRegistry appears to be running (pid ${OLD_PID}). Please stop all it before attempting to restart."
    exit 1
  fi

  # The pid in the pidfile is not running. Remove the pidfile.
  rm -f "${PID_FILE}"
}

rm "${NOHUP_FILE}" &>/dev/null
# Parse any command line arguments
while [[ $# -gt 0 ]]
do
  arg="$1"

  case $arg in
    -j)
      # Add any custom JVM settings to use when starting the HybridDB.
      JVM_SETTINGS="$2 ${JVM_SETTINGS}"
      shift
    ;;

    *)
      echo "Unknown option: ${arg}" | tee -a "${NOHUP_FILE}"  # unknown option
      exit 1
      ;;
  esac
  shift
done

checkIfIsRunning

echo "Starting the FlowRegistry..."  | tee -a "${NOHUP_FILE}"

JAVACMD=java
if [ $JAVA_HOME ];
then
	JAVACMD=$JAVA_HOME/bin/java
fi

nohup ${JAVACMD} ${DEBUG_FLAGS} ${JVM_SETTINGS} ${APP_SETTINGS} ${LOG4J_CONFIG} -cp :${CLASSPATH} com.boraydata.flowregistry.SchemaRegistryApplication >/dev/null 2>&1 &

PID=$!
echo "${PID}" > "${PID_FILE}"