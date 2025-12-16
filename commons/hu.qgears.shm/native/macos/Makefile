ifeq ($(JAVA_HOME),)
JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
endif

ifeq ($(HOMEBREW),)
HOMEBREW=$(shell brew --prefix || echo "/opt/homebrew")
endif

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin 

SO_NAME:=libshm-macos-aarch64.dylib

ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/main/resources/hu/qgears/shm/natives
endif

#TODO functions of SemaphoreNative.cpp is not supported in this port! 

all:
	$(CXX) -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE} ../malloc.c \
 		../linux/SharedMemoryNative.cpp ../jniutil.cpp ../DlMallocPoolNative.cpp \
 		../PartNativeMemoryNative.cpp 
 		
 	
clean:
	rm -fv $(OUTPUTDIR)/$(SO_NAME) 		
