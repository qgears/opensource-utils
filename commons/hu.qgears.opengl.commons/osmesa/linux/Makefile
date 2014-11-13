JNI_INCLUDE = -I/usr/lib/jvm/java-6-openjdk/include -I/usr/lib/jvm/java-6-openjdk/include/linux -I /home/rizsi/tmp/osmesa/lib/Mesa-8.0.3/include -L /home/rizsi/tmp/osmesa/lib/Mesa-8.0.3/lib
#JNI_INCLUDE = 
ARCHPOSTFIX=`uname -m |sed -e 's/x86_64/64/' -e 's/x86//' -e 's/i686//'`

gnu_c:
	g++ -o ../../src/hu/qgears/opengl/osmesa/libqosmesa$(ARCHPOSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} -Wl,-soname,libqosmesa.so \
	../OSMesa.cpp ../jniutil.cpp -lOSMesa
	gcc -fPIC -shared ${JNI_INCLUDE} ../osmesapreload.c -o libGL.so.1

