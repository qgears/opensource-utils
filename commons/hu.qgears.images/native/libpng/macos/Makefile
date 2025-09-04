JNI_ARCHPOSTFIX=aarch64
JNI_INCLUDE:=-I/Users/istvanrath/Apps/jdk/jdk-24.0.2.jdk/Contents/Home/include \
	-I /Users/istvanrath/Apps/jdk/jdk-24.0.2.jdk/Contents/Home/include/darwin \
	-I /opt/homebrew/include 
SO_NAME:=libqpng-macos-aarch64.dylib

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/main/resources/hu/qgears/images/libpng
endif

gnu_c:
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} \
	../nativeLibpng.cpp ../jniutil.cpp \
	-lpng -L /opt/homebrew/lib
	