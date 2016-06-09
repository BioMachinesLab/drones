#! /bin/sh
rsync -ru --delete ../RaspberryController pi@10.40.50.$1:/home/pi
rsync -ru --delete ../Behaviors pi@10.40.50.$1:/home/pi
rsync -ru --delete ../CommonInterface pi@10.40.50.$1:/home/pi
rsync -ru --delete ../Pi4J_IT_Version pi@10.40.50.$1:/home/pi
