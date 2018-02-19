JNI_INCLUDE = -Ideps/include/java -Ideps/include/java/win32
LIBPNG_INCLUDE = -Ideps/include/libpng -Ideps/include/zlib
LIBPNG_LIBS32 = -Ldeps/lib/win32/libpng -Ldeps/lib/win32/zlib
LIBPNG_LIBS64 = -Ldeps/lib/win64/libpng -Ldeps/lib/win64/zlib


#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/hu/qgears/images/libpng
endif

all : libqpng32 libqpng64

COMMAND = 	-D_REENTRANT -shared \
	${LIBPNG_INCLUDE} \
	${JNI_INCLUDE} -Wl,--add-stdcall-alias \
	../nativeLibpng.cpp ../jniutil.cpp \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread -lwinpthread \
	-Wl,-Bdynamic -lpng 
	
libqpng32: cp_bin32
	i686-w64-mingw32-g++  \
	-o $(OUTPUTDIR)/libqpng32.dll \
	${LIBPNG_LIBS32} \
	$(COMMAND) -lz 
 	
cp_bin32 :
	cp deps/bin/win32/* $(OUTPUTDIR)/

libqpng64 : cp_bin64
	x86_64-w64-mingw32-g++  \
	-o $(OUTPUTDIR)/libqpng64.dll \
	-D_REENTRANT -shared \
	${LIBPNG_LIBS64} \
	$(COMMAND)
 	
cp_bin64:
	cp deps/bin/win64/* $(OUTPUTDIR)/