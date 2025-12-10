ifeq ($(JAVA_HOME),)
JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
endif

ifeq ($(HOMEBREW),)
HOMEBREW=$(shell brew --prefix)
endif

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin \
	-I $(HOMEBREW)/include 

SO_NAME:=libqpng-macos-aarch64.dylib

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/main/resources/hu/qgears/images/libpng
endif

gnu_c:
	$(GCC_PREFIX)g++ -o $(OUTPUTDIR)/$(SO_NAME) \
	-install_name $(SONAME) \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} \
	../nativeLibpng.cpp ../jniutil.cpp \
	-lpng -L $(HOMEBREW)/lib
	