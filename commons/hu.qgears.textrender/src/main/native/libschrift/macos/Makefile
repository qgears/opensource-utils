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


ifeq ($(RELEASE),1)
    CONDITIONAL_FLAGS = -O2
else
    CONDITIONAL_FLAGS = -g -O0 -DDEBUG=1
endif

# Flags making hardware float arithmetic bit-exact with the Linux builds
# (see linux/Makefile for the full rationale):
#  -ffp-contract=off : never fuse a*b+c into FMA; clang fuses by default.
#  -fno-fast-math    : forbid value-changing optimizations.
# -fexcess-precision is omitted: on clang it only affects _Float16, and arm64
# has no x87-style excess precision.
DETERMINISTIC_FP_FLAGS= \
	-ffp-contract=off \
	-fno-fast-math

C_FLAGS=-fPIC -D_REENTRANT \
	$(CONDITIONAL_FLAGS) -fvisibility=hidden -std=c99 -pedantic -Wconversion \
	$(DETERMINISTIC_FP_FLAGS) \
	${JNI_INCLUDE} -I $(HOMEBREW)/include

SOURCES_LIB=../nativeLibLibschrift.c ../schrift.c ../util.c ../nativeLibLibschriftJni.c

all : ../hu_qgears_textrender_libschrift_LibschriftNative.h
	mkdir -p $(OUTPUTDIR)
	gcc -o $(OUTPUTDIR)/$(SO_NAME) \
	$(C_FLAGS) -shared \
	$(SOURCES_LIB) \
	-L $(HOMEBREW)/lib  -lm

%.s : ../%.c 
	gcc $(C_FLAGS) -fverbose-asm -S -o $@ $<


asms : nativeLibLibschrift.s schrift.s nativeLibLibschriftJni.s util.s
	@echo "Create " $<

clean:
	rm -f $(OUTPUTDIR)/$(SO_NAME) 
	rm -f *.s


