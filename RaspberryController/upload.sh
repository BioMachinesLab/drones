#! /bin/sh
rsync -ru --delete bin pi@192.168.3.1:/home/pi/RaspberryController/
rsync -ru --delete src pi@192.168.3.1:/home/pi/RaspberryController/
rsync -ru --delete Makefile pi@192.168.3.1:/home/pi/RaspberryController/
