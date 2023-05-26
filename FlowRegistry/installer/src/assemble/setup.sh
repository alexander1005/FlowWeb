#!/bin/sh

# Prints an error message to stderr and exits.
function error_exit()
{
  echo "$1" 1>&2
  exit 1
}

# Returns 0 if a DQX is running in the given directory or 1 if it isn't.
function isDqxRunning()
{
  local installPath="$1"
  local pidFile="${installPath}/FlowRegistry.pid"

  if [ ! -e "$pidFile" ]; then
    return 1
  fi

  local pid="$(cat ${pidFile})"

  if ps -p "${pid}" &> /dev/null; then
    return 0
  else
    # It's not running. Remove the pid file
    rm -f "${pidFile}" &>/dev/null
    return 1
  fi
}

echo "Running setup script..."

# The name of this file.
SETUP_FILE="$0"     # e.g.  ./setup.sh

# The name of the directory that we will install into.
INSTALL_DIR_NAME="$1"

INSTALL_PARENT_PATH="$2"

# The full path that we will be installing into.
INSTALL_PATH="${INSTALL_PARENT_PATH}/${INSTALL_DIR_NAME}"

# For debugging purposes. Usage is:
if [ ! -z ${VERBOSE+x} ]; then
  echo "Unpacking into $(pwd)"
  echo "Will install into ${INSTALL_PATH}"
fi

# Remove this setup file
rm -f "${SETUP_FILE}"
if [ "$?" -ne 0 ]; then
    error_exit "Could not remove the setup.sh script. Aborting."
fi

# Check if the install directory already exists. Will we be overwriting anything?
if [ -e "${INSTALL_PATH}" ]; then
  # Check if the existing installation is actually running.
  if isDqxRunning "${INSTALL_PATH}"; then
    EXIT_MSG="Installation aborted - A FlowRegistry is running in the installation path ${INSTALL_PATH}"
    EXIT_MSG+=$'\nPlease stop the instance before installing into this location.'
    error_exit "${EXIT_MSG}"
  fi

  # If the CLEAN variable is set then delete the existing installation dir. e.g.:
  # CLEAN=1
  if [ ! -z ${CLEAN+x} ]; then
    echo "Removing the previous installation at ${INSTALL_PATH}"
    rm -rf "${INSTALL_PATH}"
    mkdir -p "${INSTALL_PATH}"
  fi

else
  # Create the install directory
  mkdir -p "${INSTALL_PATH}"
fi


# Copy all the files and folders from the current directory into this install directory
cp -r ./* "${INSTALL_PATH}"

# create a symbolic link to the latest version installed
CURRENT_SYMLINK_DIR="${INSTALL_PARENT_PATH}/current"

# First remove the existing symbolic link, if it exists.
if [ -e "${CURRENT_SYMLINK_DIR}" ]; then
    rm -f "${CURRENT_SYMLINK_DIR}"
fi

ln -s "${INSTALL_PATH}" "${CURRENT_SYMLINK_DIR}"
if [ "$?" -ne 0 ]; then
    error_exit "Could not create the \"current\" symbolic link. Aborting."
fi


echo "All done!"