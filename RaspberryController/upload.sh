#! /bin/sh
rsync -ru --delete ../RaspberryController pi@192.168.3.$1:/home/pi
rsync -ru --delete ../Behaviors pi@192.168.3.$1:/home/pi
rsync -ru --delete ../CommonInterface pi@192.168.3.$1:/home/pi
rsync -ru --delete ../Pi4J_IT_Version pi@192.168.3.$1:/home/pi
