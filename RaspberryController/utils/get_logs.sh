#! /bin/sh
rsync -ru --delete pi@192.168.3.$1:/home/pi/RaspberryController/logs .
