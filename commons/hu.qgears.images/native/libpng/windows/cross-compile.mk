#
#
#	libqpng64.dll makefile for msys2
#
#
# prerequisits:
# jdk's include folder, JDKPATH=jdk folder (only include subfolder needed)
# msys2 64 bit mingw64 folder, MINGW64=msys2/mingw64  (only include and lib directory needed)

WINDEP = ../../../win-dep
JDKPATH=$(WINDEP)/JDK-win
MINGW_32=$(WINDEP)/mingw32
MINGW_64=$(WINDEP)/mingw64

JNI_INCLUDE=\
-I$(JDKPATH)/include \
-I$(JDKPATH)/include/win32 \


#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/hu/qgears/images/libpng
endif

COMMAND_32 =   i686-w64-mingw32-gcc -m32 -o $(OUTPUTDIR)/libqpng32.dll -I$(MINGW_32)/include -L${MINGW_32}/lib
COMMAND_64 = x86_64-w64-mingw32-gcc -m64 -o $(OUTPUTDIR)/libqpng64.dll -I$(MINGW_64)/include -L${MINGW_64}/lib

COMMAND_COMMON = -fPIC -D_REENTRANT -shared -Wl,--add-stdcall-alias ${JNI_INCLUDE} ../nativeLibpng.cpp ../jniutil.cpp \
-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread -Wl,-Bdynamic -lpng -lz.dll

.PHONY: all

all: compile32 compile64
	
compile32:
	$(COMMAND_32) $(COMMAND_COMMON) 
	
compile64:
	$(COMMAND_64) $(COMMAND_COMMON) 



