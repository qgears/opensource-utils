#JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
#JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include/linux
#POSTFIX=$(shell ../../platform_prefix.sh)${JNI_ARCHPOSTFIX}
JNI_INCLUDE:=-I/Users/istvanrath/Apps/jdk/jdk-24.0.2.jdk/Contents/Home/include \
	-I /Users/istvanrath/Apps/jdk/jdk-24.0.2.jdk/Contents/Home/include/darwin \
	-I /opt/homebrew/include \
	-I osmesa/include
JNI_ARCHPOSTFIX=aarch64
POSTFIX=macos-${JNI_ARCHPOSTFIX}

OUTPUTDIR=../../src/main/resources/hu/qgears/opengl/osmesa/

gnu_c:
	g++ -o ../../src/main/resources/hu/qgears/opengl/osmesa/libqosmesa-$(POSTFIX).dylib \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} \
	../OSMesa.cpp ../jniutil.cpp -lOSMesa -L osmesa/lib

