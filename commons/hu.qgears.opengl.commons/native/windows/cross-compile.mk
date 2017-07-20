#
#
#	libqglut(32|64).dll makefile for msys2
#
#

WINDEP = ../../win-dep
JDKPATH=$(WINDEP)/JDK-win
MINGW32=$(WINDEP)/mingw32
MINGW64=$(WINDEP)/mingw64

JNI_INCLUDE=\
-I$(JDKPATH)/include \
-I$(JDKPATH)/include/win32 \

#PKGCONFIG=`pkg-config freeglut glew --libs`
PKG_DIR_32=-I$(MINGW32)/include -L$(MINGW32)/lib
PKG_DIR_64=-I$(MINGW64)/include -L$(MINGW64)/lib
PKG_LIB=-lfreeglut -lopengl32 -lwinmm -lgdi32 -lm -lglew32

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/hu/qgears/opengl/glut
endif

COMMAND_32 =   i686-w64-mingw32-gcc -m32 -o $(OUTPUTDIR)/libqglut32.dll $(PKG_DIR_32) 
COMMAND_64 = x86_64-w64-mingw32-gcc -m64 -o $(OUTPUTDIR)/libqglut64.dll $(PKG_DIR_64) 

COMMAND_COMMON = -fPIC -D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${JNI_INCLUDE} \
	-DQGLUT_MSYS \
	../QGlut_msys.cpp ../jniutil.cpp \
	$(PKG_LIB) -lglu32 \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread 

.PHONY: all

all: compile32 compile64
	
compile32:
	$(COMMAND_32) $(COMMAND_COMMON)
	
compile64:
	$(COMMAND_64) $(COMMAND_COMMON)
