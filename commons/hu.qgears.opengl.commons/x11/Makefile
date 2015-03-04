JNI_INCLUDE = -I/usr/lib/jvm/java-6-openjdk/include -I/usr/lib/jvm/java-6-openjdk/include/linux -I/usr/lib/jvm/java-6-openjdk-amd64/include -I/usr/lib/jvm/java-6-openjdk-amd64/include/linux -I/usr/lib/jvm/java-6-sun/include -I/usr/lib/jvm/java-6-sun/include/linux -I/usr/lib/jvm/default-java/include -I/usr/lib/jvm/default-java/include/linux
ARCHPOSTFIX=`uname -m |sed -e 's/x86_64/64/' -e 's/x86//' -e 's/i686//'`

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../src/hu/qgears/opengl/x11
endif

all:
	g++ -o $(OUTPUTDIR)/libqx11gl$(ARCHPOSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} -Wl,-soname,libqx11ql.so \
	x11gl.cpp jniutil.cpp -lX11 -lGL -lGLU -lGLEW
	
#	${JNI_INCLUDE} -Wl,-soname,libqqlut.so \
#	../QGlut.cpp ../jniutil.cpp \
#	$(FREEGLUT_BINDIR)/*.o -L$(FREEGLUT_BINDIR) $(FREEGLUT_BINDIR)/libglut.a \
#	-Wl,-Bdynamic -lGLU -lGLEW
#	rm -fr ${FREEGLUT_BUILDDIR} ${FREEGLUT_BINDIR}
