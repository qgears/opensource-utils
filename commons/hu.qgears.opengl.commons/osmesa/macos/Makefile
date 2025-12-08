JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
HOMEBREW=$(shell brew --prefix)

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin \
	-I osmesa/include \
	-I $(HOMEBREW)/include


SONAME:=libqosmesa-macos-aarch64.dylib

OUTPUTDIR=../../src/main/resources/hu/qgears/opengl/osmesa

all: libs
	g++ -o $(OUTPUTDIR)/$(SONAME) \
	-install_name $(SONAME) \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} \
	../OSMesa.cpp ../jniutil.cpp -lOSMesa.8 -L osmesa/lib

libs: 
	cp osmesa/lib/libOSMesa.8.dylib $(OUTPUTDIR)/
	cp osmesa/lib/libglapi.0.dylib $(OUTPUTDIR)/
	cp osmesa/lib/libGL.1.dylib $(OUTPUTDIR)/

clean : 
	rm -fv $(OUTPUTDIR)/*.dylib