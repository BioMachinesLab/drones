#!/bin/bash
ip="192.168.3.";
user="pi"

for var in "$@"
do	
	ssh pi@$ip$var screen -S controller -p 0 -X stuff "q$(printf \\r)"
	echo $ip$var
done
echo "Done!"


