#! /bin/sh
rsync -ru --delete ../RaspberryController/bin pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru --delete ../RaspberryController/lib pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru --delete ../RaspberryController/src pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru --delete ../RaspberryController/config pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru --delete ../RaspberryController/Makefile pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru --delete ../Behaviors pi@192.168.3.$1:/home/pi
rsync -ru --delete ../CommonInterface pi@192.168.3.$1:/home/pi
rsync -ru --delete ../Pi4J_Libraries pi@192.168.3.$1:/home/pi
