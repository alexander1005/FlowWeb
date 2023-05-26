#!/usr/bin/env bash
. ./config.sh
check
DIR=`pwd`
INST_NAME=${DIR##*$FLOW_INSTALL_DIR/}
CFGDIR="$FLOW_CFG_DIR/$INST_NAME"

if [ 0"$FLOW_HOME" = "0" ]; then
    echo "Variable FLOW_HOME is not set."
    exit 1
fi

cd $FLOW_HOME/bin
. ./env.sh

Log4jOutputPath="$FLOW_LOG_DIR/$INST_NAME"
LOG4J_CONFIG="-DLog4jOutputPath=$Log4jOutputPath -Dlog4j.configurationFile=$DIR/cfg/log4j2.xml"
PIDDIR="$FLOW_STATUS_DIR/$INST_NAME"
PID_FILE="$PIDDIR/studio.pid"
NOHUP_FILE="${Log4jOutputPath}/nohup.out"
JVM_SETTINGS="-XX:+PrintFlagsFinal"
flowstudio_yml="flowstudio.yml"
flowstudio_prop="flowstudio-server.properties"
cd $DIR

if [ 0"$CFGDIR" = "0" ]; then
    echo "There is no configuration directory, please initialize."
    exit 1
fi

if [[ ! -f "$CFGDIR/cfg/$flowstudio_yml" || ! -f "$CFGDIR/cfg/$flowstudio_prop" ]];then
  echo "Please initialize studio."
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
    >&2 echo "This FlowStudio appears to be running (pid ${OLD_PID}). Please stop all it before attempting to restart."
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

echo "Starting the FlowStudio..."  | tee -a "${NOHUP_FILE}"

APP_SETTINGS="-Dspring.config.additional-location=$CFGDIR/cfg/${flowstudio_yml}"
STUDIO_JAR="$DIR/lib/*"

JAVACMD=java
if [ $JAVA_HOME ];
then
	JAVACMD=$JAVA_HOME/bin/java
fi

nohup $JAVACMD ${JVM_SETTINGS} ${APP_SETTINGS} ${LOG4J_CONFIG} -cp ${KS_CLASSPATH}:${STUDIO_JAR} com.boraydata.workflow.WorkFlowApplication >> "${NOHUP_FILE}" 2>&1 &

PID=$!
echo "${PID}" > $PID_FILE
