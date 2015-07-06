#!/bin/bash
ip="192.168.3.";
user="pi"

for var in "$@"
do	
		ssh pi@$ip$var "sudo halt;"
	echo $ip$var
done
echo "Done!"


