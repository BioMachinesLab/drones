#!/bin/bash
ip="192.168.3.";
user="pi"

for var in "$@"
do	
	ssh pi@$ip$var "killall screen run.sh; cd RaspberryController; screen -d -m -S controller ./run.sh;"
	echo $ip$var
done
echo "Done!"


