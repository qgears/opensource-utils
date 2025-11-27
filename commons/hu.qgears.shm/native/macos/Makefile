JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
HOMEBREW=$(shell brew --prefix)

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin 

SO_NAME:=libshm-macos-aarch64.dylib

ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/main/resources/hu/qgears/shm/natives
endif

#TODO functions of SemaphoreNative.cpp is not supported in this port! 

all:
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE} ../malloc.c \
 		../linux/SharedMemoryNative.cpp ../jniutil.cpp ../DlMallocPoolNative.cpp \
 		../PartNativeMemoryNative.cpp 
 		
 	
 		
