JNI_INCLUDE=-IC:/cygwin64/usr/jdkinclude -IC:/cygwin64/usr/jdkinclude/win32

ARCHPOSTFIX='64'

DLLS=CYGWIN1.DLL

#used by the maven build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../../src/hu/qgears/images/libpng
endif
	
gnu_c : cp_bin
	gcc -m64 -o $(OUTPUTDIR)/libqpng$(ARCHPOSTFIX).dll \
	-D_REENTRANT -shared -Wl,--add-stdcall-alias \
	-DM_PI='(3.14159265358979323846264338327950288419716939937510)' -D__int64=int64_t \
	${JNI_INCLUDE} \
	../../nativeLibpng.cpp ../../jniutil.cpp \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread -lwinpthread \
	-Wl,-Bdynamic -lpng -lz 
 	
cp_bin:
	cp $(addprefix C:/cygwin64/bin/, $(DLLS)) $(OUTDIR)/