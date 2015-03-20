#!/bin/bash

pid_file=/tmp/picamera.pid

if [ ! -e $pid_file ]; then
	echo "Error: The pid file " $pid_file "does not exist, looks like picamera is not running."
	exit 1
fi

pid=$(cat $pid_file)

echo "Pid of picamera was " $pid

kill $pid

echo "Killed picamera"

rm $pid_file

exit 0
