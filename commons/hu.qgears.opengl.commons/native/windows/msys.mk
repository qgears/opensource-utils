#
#
#	libqglut64.dll makefile for msys2
#
#

ifndef JDKPATH
	JDKPATH = /C/Program\ Files/Java/jdk1.8.0_60/
endif

JNI_INCLUDE= \
-I$(JDKPATH)/include  \
-I$(JDKPATH)/include/win32 \

ARCHPOSTFIX='64'

DLLS = GLEW32.DLL LIBFREEGLUT.DLL

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/hu/qgears/opengl/glut
endif

gnu_c:
	g++ -m64 -o $(OUTPUTDIR)/libqglut$(ARCHPOSTFIX).dll \
	-fPIC -D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${JNI_INCLUDE} \
	-DQGLUT_MSYS \
	../QGlut_msys.cpp ../jniutil.cpp \
	`pkg-config freeglut glew --libs` -lglu32 \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread 
	cp $(addprefix C:/msys64/mingw64/bin/,$(DLLS)) $(OUTPUTDIR)/
