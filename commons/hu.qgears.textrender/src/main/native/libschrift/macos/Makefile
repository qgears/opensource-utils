ifeq ($(JAVA_HOME),)
JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
endif

ifeq ($(HOMEBREW),)
HOMEBREW=$(shell brew --prefix || echo "/opt/homebrew")
endif

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin 

SO_NAME:=libqlibschrift-macos-aarch64.dylib

#used by the maven editor_platform_build process, see pom.xml in project.
PROJDIR= ../../../../..

ifndef OUTPUTDIR
    OUTPUTDIR = $(PROJDIR)/src/main/resources/hu/qgears/textrender/libschrift
endif


C_FLAGS=-fPIC -D_REENTRANT -shared \
	-g -std=c99 -O0 -pedantic -Wconversion $(shell pkg-config --cflags fontconfig) \
	${JNI_INCLUDE} -I $(HOMEBREW)/include

all : ../hu_qgears_textrender_libschrift_LibschriftNative.h
	mkdir -p $(OUTPUTDIR)
	gcc -o $(OUTPUTDIR)/$(SO_NAME) \
	$(C_FLAGS) \
	../natveLibLibschrift.c ../nativeLibLibschriftJni.c ../schrift.c \
	-L $(HOMEBREW)/lib $(shell pkg-config --libs fontconfig) -lm

%.s : ../%.c 
	gcc $(C_FLAGS) -fverbose-asm -S -o $@ $<


asms : nativeLibLibschrift.s schrift.s nativeLibLibschriftJni.s
	@echo "Create " $<

clean:
	rm -f $(OUTPUTDIR)/$(SO_NAME) 
	rm -f *.s


