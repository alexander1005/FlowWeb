#!/bin/sh

OUTPUT_FILE=$1
VERSION=$2
BRANCH=$3

rm -f $OUTPUT_FILE
{
echo "Build information:"
echo "  version     $VERSION"
echo "  from branch $BRANCH"
echo "  by          $(whoami)"
echo "  on          $(hostname -f)"
echo "  at          $(date)"
} >> $OUTPUT_FILE