JNI_INCLUDE= \
-IC:/Program\ Files/Java/jdk1.8.0_60/include  \
-IC:/Program\ Files/Java/jdk1.8.0_60/include/win32 \

#-IC:/cygwin64/usr/jdkinclude 
#-IC:/cygwin64/usr/jdkinclude/win32

#-DM_PI='(3.14159265358979323846264338327950288419716939937510)' -D__int64=int64_t \

ARCHPOSTFIX='64'

DLLS=CYGWIN1.DLL CYGPNG16-16.DLL CYGZ.DLL 

#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../../src/hu/qgears/images/libpng
endif
	
gnu_c : cp_bin
	gcc -m64 -mno-cygwin -o $(OUTPUTDIR)/libqpng$(ARCHPOSTFIX).dll \
	-include C:/cygwin64/usr/include/w32api/_mingw.h \
	-D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${JNI_INCLUDE} \
	../../nativeLibpng.cpp ../../jniutil.cpp \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread \
	-Wl,-Bdynamic -lpng -lz 
 	
cp_bin:
	cp $(addprefix C:/cygwin64/bin/, $(DLLS)) $(OUTPUTDIR)/