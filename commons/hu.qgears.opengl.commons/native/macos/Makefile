ifeq ($(JAVA_HOME),)
JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
endif

ifeq ($(HOMEBREW),)
HOMEBREW=$(shell brew --prefix || echo "/opt/homebrew")
endif

#assuming freeglut 3.8.0 is preinstalled with brew  - try not to use 
# brew install mesa-glu brew install mesa

JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin 

SO_NAME:=libqglut-macos-aarch64.dylib


#used by the maven editor_platform_build process, see pom.xml in project.
ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/main/resources/hu/qgears/opengl/glut
endif

all: 
	$(CXX) -o $(OUTPUTDIR)/$(SO_NAME) \
		-fPIC -D_REENTRANT -shared \
		${JNI_INCLUDE} -I $(HOMEBREW)/include \
		-DQGLUT_LOG=1 \
		../QGlut.cpp ../QGlutMac.cpp ../jniutil.cpp \
		-L $(HOMEBREW)/lib \
		-lglut -lGLU -lGL

clean:
	rm -fv $(OUTPUTDIR)/$(SO_NAME) 		

	
# 	-framework OpenGL -framework GLUT 
# Use to compile agains the build in Macos frameworks : in theory we could also use these libraries instead of freeglut, but it would require a completely different port
# glutMainLoopEvent() from freeglut can be polled. But in framework GLUT versions glutMainLoopEvent is not available, only glutMainLoop() which does not support polling (callbacks)