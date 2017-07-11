#
#
#	libqpng64.dll makefile for msys2
#
#

ifndef JDKPATH
	JDKPATH = /C/Program\ Files/Java/jdk1.8.0_60/
endif

JNI_INCLUDE= \
-I$(JDKPATH)/include  \
-I$(JDKPATH)/include/win32 \

ARCHPOSTFIX='64'

DLLS=libpng16-16.dll zlib1.dll

#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../../src/hu/qgears/images/libpng
endif
	
gnu_c : cp_bin
	gcc -m64 -o $(OUTPUTDIR)/libqpng$(ARCHPOSTFIX).dll \
	-fPIC -D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${JNI_INCLUDE} \
	../../nativeLibpng.cpp ../../jniutil.cpp \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread \
	-Wl,-Bdynamic -lpng -lz.dll 
	
cp_bin:
	cp $(addprefix /C/msys64/mingw64/bin/, $(DLLS)) $(OUTPUTDIR)/