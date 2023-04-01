#!/bin/bash
export HDFS_HOME=/user/lada17/psso
export XL_HOME=./internal_use
export DATA_HOME=$XL_HOME/dataset
export LOG=$XL_HOME/logs.txt
# export ERR=$XL_HOME/errors.txt
export ERR=$LOG
export HADOOP_CLASSPATH=$JAVA_HOME/lib/tools.jar
export N=${3-10}
export DATASET=mini

export red=`tput setaf 1`
export green=`tput setaf 2`
export yellow=`tput setaf 3`
export reset=`tput sgr0`

bash $XL_HOME/hdfs_init.sh
