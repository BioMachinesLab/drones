PROJECT= $(shell pwd)

JSOURCEPATH = -sourcepath $(PROJECT)/src/
ROOT_FILE = $(PROJECT)/src/main/Main.java

JDEST = $(PROJECT)/bin
JCC = javac
.SUFFIXES: .java .class

default:
	make clean && make build && make drone

build:
	mkdir $(JDEST) && $(JCC) -cp $(CLASSPATH) $(JSOURCEPATH) -d $(JDEST) $(ROOT_FILE)

clean:
	rm -rf $(JDEST)

drone:
	sudo java -cp $(CLASSPATH) main.Main
	
test:
	sudo java -cp $(CLASSPATH) main.Test

thymio:
	java -Djava.library.path=/usr/local/lib/jni/ -cp $(CLASSPATH) main.MainThymio
