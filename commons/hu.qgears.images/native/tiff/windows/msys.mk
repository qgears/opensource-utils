JDKPATH = /C/Program\ Files/Java/jdk1.8.0_60/

JNI_INCLUDE= \
-I$(JDKPATH)/include  \
-I$(JDKPATH)/include/win32 \

ARCHPOSTFIX='64'

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/hu/qgears/images/tiff
endif

gnu_c:
	gcc -m64 -o $(OUTPUTDIR)/qtiffloader$(ARCHPOSTFIX).dll \
	-D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${JNI_INCLUDE} \
	../tiffloader.h ../tiffloader.c ../jni/tiffloader_connector.cpp ../jni/image_data_connector.cpp ../jni/jniutil.cpp \
	-static-libgcc -lstdc++
	