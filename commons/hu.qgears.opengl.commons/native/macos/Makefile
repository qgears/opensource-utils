JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
HOMEBREW=$(shell brew --prefix)

#assuming freeglut 3.8.0 is preinstalled with brew  - try not to use 
# brew install mesa-glu brew install mesa

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin 

SO_NAME:=libqglut-macos-aarch64.dylib


#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/main/resources/hu/qgears/opengl/glut
endif

# #Add -DQGLUT_LOG to enable logging in generated binaries
# gnu_c: 
# 	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
# 		-fPIC -D_REENTRANT -shared \
# 		${JNI_INCLUDE} \
# 		-DQGLUT_LOG=1 \
# 		../QGlut.cpp ../QGlutMac.cpp ../jniutil.cpp \
# 		-framework OpenGL -framework GLUT 
	
gnu_c: 
	g++ -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE} -I $(HOMEBREW)/include \
		-DQGLUT_LOG=1 \
		../QGlut.cpp ../QGlutMac.cpp ../jniutil.cpp \
		-framework OpenGL \
		-L $(HOMEBREW)/lib \
		-lglut -lGLU -lGLEW -lGL
	
#		$(FREEGLUT_BINDIR)/*.o -L$(FREEGLUT_BINDIR) $(FREEGLUT_BINDIR)/libglut.a 

	
test : ../test.c
	gcc -g -o ../test ../test.c  \
	-I /System/Library/Frameworks/OpenGL.framework/Headers \
	-framework OpenGL -framework GLUT 