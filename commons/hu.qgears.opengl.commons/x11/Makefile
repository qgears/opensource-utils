JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux
ARCHPOSTFIX=`uname -m |sed -e 's/x86_64/64/' -e 's/x86//' -e 's/i686//'`

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../src/main/resources/hu/qgears/opengl/x11
endif

all:
	g++ -o $(OUTPUTDIR)/libqx11gl$(ARCHPOSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} -Wl,-soname,libqx11ql.so \
	x11gl.cpp jniutil.cpp -lX11 -lGL -lGLU -lGLEW
