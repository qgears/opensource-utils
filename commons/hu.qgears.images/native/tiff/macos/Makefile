JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
HOMEBREW=$(shell brew --prefix)

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin \
	-I $(HOMEBREW)/include 

SO_NAME:=qtiffloader-macos-aarch64.dylib

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/main/resources/hu/qgears/images/tiff
endif

gnu_c:
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE} \
		../tiffloader.c ../jni/tiffloader_connector.cpp \
		../jni/image_data_connector.cpp ../jni/jniutil.cpp
