#
#
#	libqpng64.dll makefile for msys2
#
#
# prerequisits:
# jdk's include folder, JDKPATH=jdk folder (only include subfolder needed)
# msys2 64 bit mingw64 folder, MINGW64=msys2/mingw64  (only include and lib directory needed)

WIN64DEP = ../../../../win64-dep
JDKPATH=$(WIN64DEP)/JDK-win
MINGW64=$(WIN64DEP)/mingw64


JNI_INCLUDE=\
-I$(JDKPATH)/include \
-I$(JDKPATH)/include/win32 \

ARCHPOSTFIX='64'
TOOLCHAIN = x86_64-w64-mingw32

#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../../src/hu/qgears/images/libpng
endif
	
gnu_c : 
	$(TOOLCHAIN)-gcc -m64 -o $(OUTPUTDIR)/libqpng$(ARCHPOSTFIX).dll \
	-fPIC -D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${JNI_INCLUDE} \
	-I${MINGW64}/include \
	-L${MINGW64}/lib \
	../../nativeLibpng.cpp ../../jniutil.cpp \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread \
	-Wl,-Bdynamic -lpng -lz.dll
