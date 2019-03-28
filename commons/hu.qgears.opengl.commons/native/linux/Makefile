JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux
ARCHPOSTFIX=`uname -m | sed -e 's/\bx86\b/i686/'`
OS_RELEASE_ID=`grep -w "ID" /etc/os-release | cut -d'=' -f2`
OS_RELEASE_VERSION=`grep -w "VERSION_ID" /etc/os-release | cut -d'=' -f2 | tr -d '\"'`
SO_NAME:=libqglut-${OS_RELEASE_ID}-${OS_RELEASE_VERSION}-${ARCHPOSTFIX}.so
# .so named in a generic way, for workspace builds in  not explicitly supported 
# distributions
SO_GENERIC_NAME:=libqglut-${ARCHPOSTFIX}.so

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/hu/qgears/opengl/glut
endif

#Add -DQGLUT_LOG to enable logging in generated binaries
gnu_c: freeglut
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE} -Wl,-soname,$(SO_NAME) \
		../QGlut.cpp ../QGlutLinux.cpp ../jniutil.cpp \
		$(FREEGLUT_BINDIR)/*.o -L$(FREEGLUT_BINDIR) $(FREEGLUT_BINDIR)/libglut.a \
		-Wl,-Bdynamic -lGLU -lGLEW
	rm -fr ${FREEGLUT_BUILDDIR} ${FREEGLUT_BINDIR}

freeglut: init
	/bin/sh buildfreeglut.sh $(FREEGLUT_BUILDDIR) $(FREEGLUT_BINDIR)
	cp -f $(OUTPUTDIR)/$(SO_NAME) $(OUTPUTDIR)/$(SO_GENERIC_NAME)
	
init:
FREEGLUT_BUILDDIR := $(shell mktemp -d /tmp/freeglut_XXXXXX)
FREEGLUT_BINDIR := ${FREEGLUT_BUILDDIR}-bin
