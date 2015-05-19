#! /bin/sh
rsync -ru  ../../RaspberryController/bin pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru  ../../RaspberryController/lib pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru  ../../RaspberryController/src pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru  ../../RaspberryController/config pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru  ../../RaspberryController/Makefile pi@192.168.3.$1:/home/pi/RaspberryController
rsync -ru  ../../Behaviors pi@192.168.3.$1:/home/pi
rsync -ru  ../../CommonInterface pi@192.168.3.$1:/home/pi
rsync -ru  ../../Pi4J_Libraries pi@192.168.3.$1:/home/pi
