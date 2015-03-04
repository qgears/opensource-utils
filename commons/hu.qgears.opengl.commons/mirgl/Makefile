JNI_INCLUDE = -I/usr/lib/jvm/default-java/include -I/usr/lib/jvm/default-java/include/linux
ARCHPOSTFIX=`uname -m |sed -e 's/x86_64/64/' -e 's/x86//' -e 's/i686//'`

#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../src/hu/qgears/opengl/mirgl
endif

all:
	g++ -o $(OUTPUTDIR)/libmirgl$(ARCHPOSTFIX).so \
	-fPIC -D_REENTRANT -shared \
	${JNI_INCLUDE} -I/usr/include/mirclient -I/usr/include/mircommon \
	-Wl,-soname,libmirgl.so \
	mirgl.cpp jniutil.cpp -lEGL -lGL -lGLU -lmirclient
