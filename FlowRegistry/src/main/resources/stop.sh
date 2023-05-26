#!/bin/bash --login
. ./config.sh
check

DIR=`pwd`
INST_NAME=${DIR##*$FLOW_INSTALL_DIR/}
PIDFILE="$FLOW_STATUS_DIR/$INST_NAME/registry.pid"
echo "Stopping the Registry."

if [ -f "${PIDFILE}" ];
then

  PID=$(cat "${PIDFILE}")
  kill "${PID}" > /dev/null 2>&1
  rm -f "${PIDFILE}"

else
  echo "PID file missing, so stop nothing."
  exit 0
fi

echo "The Registry has been stopped."

