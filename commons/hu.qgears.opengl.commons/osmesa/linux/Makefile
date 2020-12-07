JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include/linux
POSTFIX=$(shell ../../platform_prefix.sh)${JNI_ARCHPOSTFIX}

OUTPUTDIR=../../src/hu/qgears/opengl/osmesa/

gnu_c:
	g++ -o ../../src/hu/qgears/opengl/osmesa/libqosmesa-$(POSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} -Wl,-soname,libqosmesa.so \
	../OSMesa.cpp ../jniutil.cpp -lOSMesa
	gcc -fPIC -shared ${JNI_INCLUDE} ../osmesapreload.c -o libGL.so.1
	mkdir -p ${OUTPUTDIR}
	cp libGL.so.1 ${OUTPUTDIR}/libGL-$(POSTFIX).so.1

