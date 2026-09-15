ifeq ($(JAVA_HOME),)
JAVA_HOME=$(shell java -XshowSettings:properties -version 2>&1 | grep java.home | cut -d '=' -f2 | xargs echo)
endif

ifeq ($(HOMEBREW),)
HOMEBREW=$(shell brew --prefix || echo "/opt/homebrew")
endif


JNI_INCLUDE:=-I$(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin 


SO_NAME:=libqstbtruetype-macos-aarch64.dylib

#used by the maven editor_platform_build process, see pom.xml in project.
PROJDIR= ../../../../..

ifndef OUTPUTDIR
    OUTPUTDIR = $(PROJDIR)/src/main/resources/hu/qgears/textrender/stbtt
endif

SOURCES:=../nativeLibStbttJni.c ../qstb_glyphreader.c ../stb_truetype.c ../nativeLibStbtt.c ../qstb_ctype.c ../qstb_line.c

C_FLAGS=-std=c99 -O3 \
 -fPIC -D_REENTRANT -shared  \
 $(shell pkg-config --cflags fontconfig) \
 ${JNI_INCLUDE} -I $(HOMEBREW)/include \

all : ../hu_qgears_textrender_stbtt_StbTrueTypeNative.h $(SOURCES)
	mkdir -p $(OUTPUTDIR)
	gcc $(C_FLAGS) \
	-o $(OUTPUTDIR)/$(SO_NAME) \
	${SOURCES} \
	-L $(HOMEBREW)/lib $(shell pkg-config --libs fontconfig) -lm
	

%.s : ../%.c 
	gcc $(C_FLAGS) -fverbose-asm -S -o $@ $<

asms : nativeLibStbttJni.s qstb_glyphreader.s stb_truetype.s nativeLibStbtt.s qstb_ctype.s qstb_line.s
	@echo "Create " $<

clean:
	rm -f $(OUTPUTDIR)/$(SO_NAME) $(OUTPUTDIR)/$(SO_GENERIC_NAME)
	rm -f *.s


