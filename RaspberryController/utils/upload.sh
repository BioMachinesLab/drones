#!/bin/bash

ip="192.168.3.";
user="pi"
total=9
increment=$(echo "scale=1; 100/$total" |bc)

function log() {
	echo -ne "\t$ip$1 ["
	for ((n=1; n<=$3; n++))
	do
		if [ "$n" -le "$2" ]; then
	    	echo -ne "#"
	    else
	    	echo -ne " "
	    fi
	done
	
	current=$(echo "scale=0; $2*$increment" |bc)
	
	echo -ne "] $current% \r"
}

for var in "$@"
do	
	log $var 0 $total
	rsync -ru  ../../RaspberryController/bin $user@$ip$var:/home/$user/RaspberryController
	log $var 1 $total
	rsync -ru  ../../RaspberryController/lib $user@$ip$var:/home/$user/RaspberryController
	log $var 2 $total
	rsync -ru  ../../RaspberryController/src $user@$ip$var:/home/$user/RaspberryController
	log $var 3 $total
	rsync -ru  ../../RaspberryController/config $user@$ip$var:/home/$user/RaspberryController
	log $var 4 $total
	rsync -ru  ../../RaspberryController/Makefile $user@$ip$var:/home/$user/RaspberryController
	log $var 5 $total
	rsync -ru  ../../Behaviors $user@$ip$var:/home/$user
	log $var 6 $total
	rsync -ru  ../../CommonInterface $user@$ip$var:/home/$user
	log $var 7 $total
	rsync -ru  ../../Pi4J_Libraries $user@$ip$var:/home/$user
	log $var 8 $total
	rsync -ru  ../../RaspberryController/run.sh $user@$ip$var:/home/$user/RaspberryController
	log $var 9 $total
	echo
done
echo "Done!"