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
SO_NAME:=libkmsjni-${POSTFIX}.so


#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../src/main/resources/hu/qgears/opengl/kms
endif

OUTPUT_JAWT = $(OUTPUTDIR)/../fakejawt

gnu_c:
	echo $(POSTFIX)
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} -Wl,-soname,$(SO_NAME) \
 		jniutil.cpp \
 		modeset_vsync_api.cpp kms_jni.cpp \
 		`pkg-config --cflags --libs libdrm` -I.
	mkdir -p $(OUTPUT_JAWT)
	g++ -o $(OUTPUT_JAWT)/libjawt-$(POSTFIX).so \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE} -Wl,-soname,libjawt.so \
 		jawt_dummy.cpp \
 		-I.

drmScreenshot: drmScreenshot.cpp
	gcc -o drmScreenshot drmScreenshot.cpp \
		`pkg-config --cflags --libs libdrm` \
		`pkg-config cairomm-1.0 pangocairo pangomm-1.4 --cflags --libs`

