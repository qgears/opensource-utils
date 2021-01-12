JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include/linux
    
SO_NAME:=libinputjni-`../platform_prefix.sh`${JNI_ARCHPOSTFIX}.so


#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../src/hu/qgears/opengl/libinput
endif

gnu_c:
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} -Wl,-soname,$(SO_NAME) \
 		jniutil.cpp \
 		libinput_jni.cpp \
 		-lrt \
 		`pkg-config --cflags --libs libudev` \
 		`pkg-config --cflags --libs libinput` -I.
