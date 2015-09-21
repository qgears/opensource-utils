JNI_INCLUDE = -Ideps/java-win/include -Ideps/java-win/include/win32
LIBPNG_INCLUDE = -Ideps/libpng-win/include -Ideps/zlib-win/include
LIBPNG_LIBS = -Ldeps/libpng-win/lib -Ldeps/zlib-win/lib

ARCHPOSTFIX='32'

#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/hu/qgears/images/libpng
endif
	
gnu_c: cp_bin
	i686-w64-mingw32-g++  \
	-o $(OUTPUTDIR)/libqpng$(ARCHPOSTFIX).dll \
	-D_REENTRANT -shared \
	${LIBPNG_INCLUDE} \
	${JNI_INCLUDE} -Wl,--add-stdcall-alias \
	../nativeLibpng.cpp ../jniutil.cpp \
	${LIBPNG_LIBS} \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread -lwinpthread \
	-Wl,-Bdynamic -lpng -lz 
 	
cp_bin:
	cp deps/bin/* $(OUTPUTDIR)/
