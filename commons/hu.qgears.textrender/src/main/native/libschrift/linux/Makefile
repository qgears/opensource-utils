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

C_FLAGS=-fPIC -D_REENTRANT \
	-g -std=c99 -O0 -pedantic -Wconversion  \
	${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE}

# sources shared by the shared library and the standalone manual-test programs
SOURCES=../nativeLibLibschrift.c ../schrift.c ../util.c
SOURCES_LIB=$(SOURCES) ../nativeLibLibschriftJni.c

# flags shared by the standalone manual-test programs
TESTPROG_FLAGS=-DINCLUDE_MAIN=1 \
	-fno-fast-math \
	-ffp-contract=off \
	-fexcess-precision=standard

.DEFAULT_GOAL := all
.PHONY: all lib bversion testprog asms clean

all : lib bversion testprog

lib : ../hu_qgears_textrender_libschrift_LibschriftNative.h ../hu_qgears_textrender_TrueTypeRenderer.h
	mkdir -p $(OUTPUTDIR)
	gcc -o $(OUTPUTDIR)/$(SO_NAME) \
	$(C_FLAGS) -shared \
	 -Wl,-soname,$(SO_NAME) \
	 $(SOURCES_LIB) \
	 -lm
	cp -f $(OUTPUTDIR)/$(SO_NAME) $(OUTPUTDIR)/$(SO_GENERIC_NAME)

bversion:
	gcc -DSOFTFLOAT=1 \
	$(TESTPROG_FLAGS) \
	-o libschrift_manualtest_softfloat.exe \
	$(C_FLAGS) \
	 $(SOURCES) \
	 -lm

testprog:
	gcc \
	$(TESTPROG_FLAGS) \
	-o libschrift_manualtest.exe \
	$(C_FLAGS) \
	 $(SOURCES) \
	 -lm

../hu_qgears_textrender_libschrift_LibschriftNative.h : 
	javac -cp $(JNIDEPS) \
	-h $(JNI_HEADERS) $(PROJDIR)/src/main/java/hu/qgears/textrender/libschrift/LibschriftNative.java 

../hu_qgears_textrender_TrueTypeRenderer.h :
	javac -cp $(JNIDEPS) \
	-h $(JNI_HEADERS) $(PROJDIR)/src/main/java/hu/qgears/textrender/TrueTypeFont.java

%.s : ../%.c 
	gcc $(C_FLAGS) -fverbose-asm -S -o $@ $<


asms : $(notdir $(SOURCES_LIB:.c=.s))
	@echo "Create " $<

clean:
	rm -f $(OUTPUTDIR)/$(SO_NAME) $(OUTPUTDIR)/$(SO_GENERIC_NAME)
	rm -f *.s
	rm -f *.exe


