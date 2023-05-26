#!/usr/bin/env bash
target="$0"

HADOOP_CLASSPATH2=`hadoop classpath`
HADOOP_CLASSPATH=""
array=(${HADOOP_CLASSPATH2//:/ })
num=${#array[*]}
for q in $(seq ${num} -1 0)
do
  dir=${array[$q]}
  if [[ $dir =~ "servlet-api" ]]; then
      continue
  fi
  if [[ $dir =~ "gson" ]]; then
      continue
  fi
  HADOOP_CLASSPATH="${dir}:$HADOOP_CLASSPATH"
done

# Convert relative path to absolute path
bin=`dirname "$target"`
. "$bin"/config.sh

CC_CLASSPATH=`constructFlinkClassPath`
KS_CLASSPATH=$CC_CLASSPATH:$INTERNAL_HADOOP_CLASSPATHS