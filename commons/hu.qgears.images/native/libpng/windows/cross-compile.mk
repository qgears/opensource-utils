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
MINGW32=$(WINDEP)/mingw32
MINGW64=$(WINDEP)/mingw64

JNI_INCLUDE=\
-I$(JDKPATH)/include \
-I$(JDKPATH)/include/win32 \


#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/hu/qgears/images/libpng
endif

COMMAND_32 =   i686-w64-mingw32-gcc -m32 -o $(OUTPUTDIR)/libqpng32.dll -I$(MINGW32)/include -L${MINGW32}/lib
COMMAND_64 = x86_64-w64-mingw32-gcc -m64 -o $(OUTPUTDIR)/libqpng64.dll -I$(MINGW64)/include -L${MINGW64}/lib

COMMAND_COMMON = -fPIC -D_REENTRANT -shared -Wl,--add-stdcall-alias ${JNI_INCLUDE} ../nativeLibpng.cpp ../jniutil.cpp \
-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread -Wl,-Bdynamic -lpng -lz.dll

.PHONY: all compile32 compile64 checkdir

all: compile32 compile64
	
checkdir:
	echo "Checking directories needed for cross-compile..."
	if [ ! -d $(MINGW32) ]; then echo "[ERROR]\n[ERROR]: mingw32 directory does not exist\n[ERROR]"; exit -1 ; fi
	if [ ! -d $(MINGW64) ]; then echo "[ERROR]\n[ERROR]: mingw64 directory does not exist\n[ERROR]"; exit -1 ; fi
	if [ ! -d $(JDKPATH) ]; then echo "[ERROR]\n[ERROR]: jdk directory does not exist\n[ERROR]";exit -1 ; fi

compile32: checkdir
	$(COMMAND_32) $(COMMAND_COMMON)
	
compile64: checkdir
	$(COMMAND_64) $(COMMAND_COMMON)

