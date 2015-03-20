PROJECT= $(shell pwd)

JSOURCEPATH = -sourcepath $(PROJECT)/src/
ROOT_FILE = $(PROJECT)/src/main/Main.java

JDEST = $(PROJECT)/bin
JCC = javac
.SUFFIXES: .java .class

default:
	make clean && make build && make run

build:
	mkdir $(JDEST) && $(JCC) -cp $(CLASSPATH) $(JSOURCEPATH) -d $(JDEST) $(ROOT_FILE)

clean:
	rm -rf $(JDEST)

drone:
	java -cp $(CLASSPATH) main.Main

thymio:
	java -Djava.library.path=/usr/local/lib/jni/ -cp $(CLASSPATH) main.MainThymio