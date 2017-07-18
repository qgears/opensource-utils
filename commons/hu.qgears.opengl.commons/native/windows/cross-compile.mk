#
#
#	libqglut64.dll makefile for msys2
#
#

WIN64DEP = ../../win64-dep
JDKPATH=$(WIN64DEP)/JDK-win
MINGW64=$(WIN64DEP)/mingw64


JNI_INCLUDE=\
-I$(JDKPATH)/include \
-I$(JDKPATH)/include/win32 \

ARCHPOSTFIX='64'
TOOLCHAIN = x86_64-w64-mingw32

#PKGCONFIG=`pkg-config freeglut glew --libs`
PKGCONFIG=-L$(MINGW64)/lib -lfreeglut -lopengl32 -lwinmm -lgdi32 -lm -lglew32

DLLS = GLEW32.DLL LIBFREEGLUT.DLL

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/hu/qgears/opengl/glut
endif

gnu_c:
	$(TOOLCHAIN)-gcc -m64 -o $(OUTPUTDIR)/libqglut$(ARCHPOSTFIX).dll \
	-fPIC -D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${JNI_INCLUDE} \
	-I$(MINGW64)/include \
	-DQGLUT_MSYS \
	../QGlut_msys.cpp ../jniutil.cpp \
	$(PKGCONFIG) -lglu32 \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread 

