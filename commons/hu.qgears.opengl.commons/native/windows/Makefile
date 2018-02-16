TOOL32 = i686-w64-mingw32
TOOL64 = x86_64-w64-mingw32

#Freeglut sources and config
FG_OUTDIR := $(shell mktemp -d /tmp/win_freeglut_XXXXXX)
FG_SRC = ../freeglut-2.6.0
FG_DEFFILE = $(FG_SRC)/src/freeglutdll.def
fgsources=$(wildcard $(FG_SRC)/src/*.c)
fgobjs64=$(fgsources:.c=.64.o)
fgobjs32=$(fgsources:.c=.32.o)
FG_CFLAGS=-O2 -DTARGET_HOST_MS_WINDOWS -DFREEGLUT_EXPORTS -I. -I$(FG_SRC)/include
FG_LDFLAGS=-lopengl32 -lgdi32 -lwinmm
FG_LIB_DIR32 = $(FG_OUTDIR)/w32/lib
FG_BIN_DIR32 = $(FG_OUTDIR)/w32/bin
FG_LIB_DIR64 = $(FG_OUTDIR)/w64/lib
FG_BIN_DIR64 = $(FG_OUTDIR)/w64/bin

#QGlut config
#used by the maven editor_platform_build process, see pom.xml in project.

QGLUT_INC=\
-I./deps/include/java \
-I./deps/include/java/win32 \
-I$(FG_SRC)/include \
-I./deps/include/


QGLUT_LIB_32=-L$(FG_LIB_DIR32) -L./deps/w32/lib
QGLUT_LIB_64=-L$(FG_LIB_DIR64) -L./deps/w64/lib 

ifndef OUTPUTDIR
    OUTPUTDIR = ../../src/hu/qgears/opengl/glut
endif

COMMAND_32 =   $(TOOL32)-gcc -m32 -o $(OUTPUTDIR)/libqglut32.dll $(QGLUT_LIB_32) 
COMMAND_64 = $(TOOL64)-gcc -m64 -o $(OUTPUTDIR)/libqglut64.dll $(QGLUT_LIB_64) 
COMMAND_COMMON = -D_REENTRANT -shared -Wl,--add-stdcall-alias \
	${QGLUT_INC} \
	-DQGLUT_MSYS \
	../QGlut_msys.cpp ../jniutil.cpp \
	-lopengl32 -lwinmm -lgdi32 -lm -lglew32 -lglu32  -lfreeglut-gen \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++ -lpthread 


.PHONY: all init qglut32 qglut64 freeglut32 freegult64

all: qglut64 qglut32

qglut32: freeglut32
	$(COMMAND_32) $(COMMAND_COMMON)
	$(RM) $(fgobjs32)

qglut64: freeglut64
	$(COMMAND_64) $(COMMAND_COMMON)
	$(RM) $(fgobjs64)

$(fgobjs32): %.32.o: %.c 
	$(TOOL32)-gcc -m32 -c $(FG_CFLAGS) $< -o $@
 
$(fgobjs64): %.64.o: %.c
	$(TOOL64)-gcc -m64 -c $(FG_CFLAGS) $< -o $@


freeglut32: init $(fgobjs32)
	$(TOOL32)-gcc -m32 -shared $(fgobjs32) $(FG_LDFLAGS) -o $(FG_BIN_DIR32)/freeglut.dll -Wl,--out-implib,$(FG_LIB_DIR32)/libfreeglut-gen.dll.a
	$(TOOL32)-dlltool --dllname $(FG_BIN_DIR32)/freeglut.dll --input-def $(FG_DEFFILE) --output-lib $(FG_LIB_DIR32)/libfreeglut.dll.a
	
freeglut64: init $(fgobjs64)
	$(TOOL64)-gcc -m64 -shared $(fgobjs64) $(FG_LDFLAGS) -o $(FG_BIN_DIR64)/freeglut.dll -Wl,--out-implib,$(FG_LIB_DIR64)/libfreeglut-gen.dll.a
	$(TOOL64)-dlltool --dllname $(FG_BIN_DIR64)/freeglut.dll --input-def $(FG_DEFFILE) --output-lib $(FG_LIB_DIR64)/libfreeglut.dll.a

init: 
	mkdir -p $(FG_BIN_DIR32)
	mkdir -p $(FG_LIB_DIR32)
	mkdir -p $(FG_BIN_DIR64)
	mkdir -p $(FG_LIB_DIR64)

