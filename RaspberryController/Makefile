PROJECT= /home/pi/scripts/java/RaspberryController

CLASSPATH = -cp .:classes:/opt/pi4j/lib/'*':$(PROJECT)/lib/'*'
JSOURCEPATH = -sourcepath $(PROJECT)/src/
ROOT_FILE = $(PROJECT)/src/main/Controller.java

JDEST = ./bin
JCC = javac
.SUFFIXES: .java .class

default: 
	make clean && make all && make run

all:
	mkdir $(JDEST) && $(JCC) $(CLASSPATH) $(JSOURCEPATH) -d $(JDEST) $(ROOT_FILE)

clean:
	rm -rf $(JDEST)

run:
	cd $(JDEST) && sudo java $(CLASSPATH) main/Controller
