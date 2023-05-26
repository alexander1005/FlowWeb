#!/bin/bash --login
. ./config.sh
check
DIR=`pwd`
INST_NAME=${DIR##*$FLOW_INSTALL_DIR/}
CFGDIR="$FLOW_CFG_DIR/$INST_NAME"

PIDFILE="$FLOW_STATUS_DIR/$INST_NAME/studio.pid"

echo "Stopping the Studio..."

if [ -f "${PIDFILE}" ];
then
  PID=$(cat "${PIDFILE}")
  kill "${PID}" > /dev/null 2>&1
  rm -f "${PIDFILE}"
else
  echo "PID file missing, so stop nothing."
  exit 0;
fi

echo "The Studio has been stopped."