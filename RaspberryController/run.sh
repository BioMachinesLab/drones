#!/bin/bash
export CLASSPATH=/home/pi/RaspberryController/bin:/home/pi/CommonInterface/bin:/home/pi/Behaviors/bin:.:/home/pi/CommonInterface/lib/joda-time-2.4.jar:/opt/pi4j/lib/pi4j-core.jar:/opt/pi4j/lib/pi4j-device.jar:/opt/pi4j/lib/pi4j-gpio-extension.jar:/opt/pi4j/lib/pi4j-service.jar:/home/pi/CommonInterface/jcoord-1.0.jar:/home/pi/CommonInterface/lib/JKalman.jar
make drone
