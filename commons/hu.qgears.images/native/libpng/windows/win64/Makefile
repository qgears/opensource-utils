JNI_INCLUDE = -I../deps/include/java -I../deps/include/java/win32
LIBPNG_INCLUDE = -I../deps/include/libpng -I../deps/include/zlib
LIBPNG_LIBS = -L../deps/lib/win64/libpng -L../deps/lib/win64/zlib

ARCHPOSTFIX='64'

#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../../src/hu/qgears/images/libpng
endif
	
gnu_c : cp_bin
	x86_64-w64-mingw32-g++  \
	-o $(OUTPUTDIR)/libqpng$(ARCHPOSTFIX).dll \
	-D_REENTRANT -shared \
	${LIBPNG_INCLUDE} \
	${JNI_INCLUDE} -Wl,--add-stdcall-alias \
	../../nativeLibpng.cpp ../../jniutil.cpp \
	${LIBPNG_LIBS} \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread -lwinpthread \
	-Wl,-Bdynamic -lpng -lz 
 	
cp_bin:
	cp ../deps/bin/win64/* $(OUTPUTDIR)/
