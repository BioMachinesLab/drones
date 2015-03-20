#!/bin/bash

pid_file=/tmp/picamera.pid

if [ -e $pid_file ]; then
	echo "Error: The pid file " $pid_file " exists, looks like picamera is already running."
	echo "If this is not the case delete the file."
	exit 1
fi

./picamera/server_picamera.py & pid=$!

echo "Pid of picamera is " $pid

echo $pid > $pid_file

exit 0
