#!/usr/bin/env bash

. ./config.sh
check

DIR=$(dirname "$0")
CURRENT_DIR=`pwd`

INST_NAME=${CURRENT_DIR##*$FLOW_INSTALL_DIR/}
CFGDIR="$FLOW_CFG_DIR/$INST_NAME"

jarPath="$DIR/install/installation-shaded.jar"
propertiesPath="$CFGDIR/registry.properties"

if [ 0"$CFGDIR" = "0" ]; then
    echo "There is no configuration directory, please initialize."
    exit 1
fi

if [[ ! -f "$propertiesPath" ]];then
  echo "There is no configuration file,Please initialize."
  exit 1
fi

if [ ! -d "$CFGDIR/cfg" ]; then
  mkdir -p "$CFGDIR/cfg"
fi

cd $DIR

JAVACMD=java
if [ $JAVA_HOME ];
then
	JAVACMD=$JAVA_HOME/bin/java
fi
$JAVACMD -cp $jarPath com.boraydata.flow.install.Installation -a registry -p $propertiesPath --path $CFGDIR
