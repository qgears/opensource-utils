JNI_INCLUDE:=-I../../libpng/windows/deps/include/java -I../../libpng/windows/deps/include/java/win32

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/hu/qgears/images/tiff
endif
all: tiffwin32 tiffwin64

COMMAND = -D_REENTRANT -shared -Wl,--add-stdcall-alias ${JNI_INCLUDE}  \
	../tiffloader.h ../tiffloader.c ../jni/tiffloader_connector.cpp ../jni/image_data_connector.cpp ../jni/jniutil.cpp \
	-static-libgcc -static-libstdc++

tiffwin32 :
	i686-w64-mingw32-g++ -m32 -o $(OUTPUTDIR)/qtiffloader.dll -Wl,-soname,qtiffloader.dll \
	$(COMMAND)
	
tiffwin64 :
	x86_64-w64-mingw32-g++ -m64 -o $(OUTPUTDIR)/qtiffloader64.dll -Wl,-soname,qtiffloader64.dll \
	$(COMMAND)	