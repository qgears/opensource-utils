JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.11.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.11.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/default-java/include \
    -I/usr/lib/jvm/default-java/include/linux
ARCHPOSTFIX=`uname -m | sed -e 's/\bx86\b/i686/'`
OS_RELEASE_ID=`grep -w "ID" /etc/os-release | cut -d'=' -f2`
OS_RELEASE_VERSION=`grep -w "VERSION_ID" /etc/os-release | cut -d'=' -f2 | tr -d '\"'`
SO_NAME:=libqlibschrift-${OS_RELEASE_ID}-${OS_RELEASE_VERSION}-${ARCHPOSTFIX}.so
# .so named in a generic way, for workspace builds in  not explicitly supported 
# distributions
SO_GENERIC_NAME:=libqlibschrift-${ARCHPOSTFIX}.so



#used by the maven editor_platform_build process, see pom.xml in project.
PROJDIR= ../../../../..

ifndef OUTPUTDIR
    OUTPUTDIR = $(PROJDIR)/src/main/resources/hu/qgears/textrender/libschrift
endif


JNIDEPS=$(PROJDIR)/target/classes:$(PROJDIR)/../hu.qgears.images/target/classes:$(PROJDIR)/../hu.qgears.commons/target/classes:$(PROJDIR)/../hu.qgears.nativeloader/target/classes

JNI_HEADERS :=  $(PROJDIR)/src/main/native/libschrift

all : ../hu_qgears_textrender_libschrift_LibschriftNative.h
	mkdir -p $(OUTPUTDIR)
	gcc -o $(OUTPUTDIR)/$(SO_NAME) \
	-fPIC -D_REENTRANT -shared \
	-g -std=c99 -pedantic -Wconversion \
	${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE} -Wl,-soname,$(SO_NAME) \
	../nativeLibLibschrift.c ../nativeLibLibschriftJni.c ../schrift.c
	cp -f $(OUTPUTDIR)/$(SO_NAME) $(OUTPUTDIR)/$(SO_GENERIC_NAME)

../hu_qgears_textrender_libschrift_LibschriftNative.h : 
	javac -cp $(JNIDEPS) \
	-h $(JNI_HEADERS) $(PROJDIR)/src/main/java/hu/qgears/textrender/libschrift/LibschriftNative.java

clean:
	rm -f $(OUTPUTDIR)/$(SO_NAME) $(OUTPUTDIR)/$(SO_GENERIC_NAME)


