#!/bin/bash

function check() {
    if [ ! -d "$FLOW_INSTALL_DIR" ]; then
        echo "Please configurate the FLOW Env."
        exit 1
    fi
    if [ ! -d "$FLOW_CFG_DIR" ]; then
        echo "Please configurate the FLOW Env."
        exit 1
    fi
    if [ ! -d "$FLOW_LOG_DIR" ]; then
        echo "Please configurate the FLOW Env."
        exit 1
    fi
    if [ ! -d "$FLOW_STATUS_DIR" ]; then
        echo "Please configurate the FLOW Env."
        exit 1
    fi
}