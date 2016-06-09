#!/bin/bash
ip="192.168.3.";
user="pi"

mkdir logs

for var in "$@"
do	
	mkdir logs/$var
	rsync -ru pi@$ip$var:/home/pi/RaspberryController/logs/* logs/$var
	echo $ip$var
done
echo "Done!"
