JNI_INCLUDE = -I/usr/lib/jvm/java-6-openjdk/include -I/usr/lib/jvm/java-6-openjdk/include/linux \
	-I/usr/lib/jvm/java-6-openjdk-amd64/include -I/usr/lib/jvm/java-6-openjdk-amd64/include/linux
ARCHPOSTFIX=`uname -m |sed -e 's/x86_64/64/' -e 's/x86//' -e 's/i686//'`

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/hu/qgears/shm/natives
endif
gnu_c:
	g++ -o $(OUTPUTDIR)/libshm$(ARCHPOSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} -Wl,-soname,libshm$(ARCHPOSTFIX).so ../malloc.c \
	 SharedMemoryNative.cpp ../jniutil.cpp ../DlMallocPoolNative.cpp \
	 ../PartNativeMemoryNative.cpp SemaphoreNative.cpp \
	 -lrt
	 

