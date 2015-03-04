JNI_INCLUDE = -I/usr/lib/jvm/java-6-openjdk/include -I/usr/lib/jvm/java-6-openjdk/include/linux -I/usr/lib/jvm/java-6-openjdk-amd64/include -I/usr/lib/jvm/java-6-openjdk-amd64/include/linux -I/usr/lib/jvm/java-6-sun/include -I/usr/lib/jvm/java-6-sun/include/linux -I/usr/lib/jvm/default-java/include -I/usr/lib/jvm/default-java/include/linux
#JNI_INCLUDE = 
ARCHPOSTFIX=`uname -m |sed -e 's/x86_64/64/' -e 's/x86//'`

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../../src/hu/qgears/images/devil
endif
gnu_c:
	g++ -o $(OUTPUTDIR)/libdevil$(ARCHPOSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} -Wl,-soname,libdevil$(ARCHPOSTFIX).so \
	../nativeDevil.cpp ../jniutil.cpp \
	`pkg-config --cflags --libs IL`
	
	