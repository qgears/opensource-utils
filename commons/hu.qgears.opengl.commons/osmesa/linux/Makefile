JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux
ARCHPOSTFIX=`uname -m |sed -e 's/x86_64/64/' -e 's/x86//' -e 's/i686//'`

gnu_c:
	g++ -o ../../src/hu/qgears/opengl/osmesa/libqosmesa$(ARCHPOSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} -Wl,-soname,libqosmesa.so \
	../OSMesa.cpp ../jniutil.cpp -lOSMesa
	gcc -fPIC -shared ${JNI_INCLUDE} ../osmesapreload.c -o libGL.so.1
	cp libGL.so.1 ../../src/hu/qgears/opengl/osmesa/libGL.so.1

