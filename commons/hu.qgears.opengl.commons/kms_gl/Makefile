JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-11-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/default-java/include \
    -I/usr/lib/jvm/default-java/include/linux
    
POSTFIX:=$(shell ../platform_prefix.sh)${JNI_ARCHPOSTFIX}
    
SO_NAME:=libkmsgljni-${POSTFIX}.so


#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../src/main/resources/hu/qgears/opengl/kmsgl
endif

gnu_c:
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} -Wl,-soname,$(SO_NAME) \
 		drm-legacy.c drm-common.c common.c psplash-console.c jniutil.cpp \
 		kms_gl_jni.cpp \
 		`pkg-config --cflags --libs libdrm` `pkg-config --cflags --libs gbm` -lEGL -lGL -I.


