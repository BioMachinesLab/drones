#! /bin/sh
rsync -ru --delete bin pi@192.168.3.1:/home/pi/RaspberryController/
rsync -ru --delete src pi@192.168.3.1:/home/pi/RaspberryController/
rsync -ru --delete Makefile pi@192.168.3.1:/home/pi/RaspberryController/
rsync -ru --delete io_config.conf pi@192.168.3.1:/home/pi/RaspberryController/
rsync -ru --delete logs/fakegps.log pi@192.168.3.1:/home/pi/RaspberryController/logs/
rsync -ru --delete ../Behaviors/bin pi@192.168.3.1:/home/pi/Behaviors/
rsync -ru --delete ../CommonInterface/bin pi@192.168.3.1:/home/pi/CommonInterface/
rsync -ru --delete ../CommonInterface/jcoord-1.0.jar pi@192.168.3.1:/home/pi/CommonInterface/
rsync -ru --delete ../Pi4J_IT_Version/bin pi@192.168.3.1:/home/pi/Pi4J_IT_Version/
