FREEGLUT = freeglut-2.8.0

DEFFILE = ./$(FREEGLUT)/src/freeglutdll.def
OUTDIR = ../win-dep

TOOL32 = i686-w64-mingw32
TOOL64 = x86_64-w64-mingw32


sources=$(wildcard $(FREEGLUT)/src/*.c) $(wildcard $(FREEGLUT)/src/mswin/*.c) $(wildcard $(FREEGLUT)/src/util/*.c)
objs32=$(sources:.c=.32.o)
objs64=$(sources:.c=.64.o)

.PHONY: pre all clean dll32 dll64 importlib32 importlib64 copy-include32 copy-include64

all: all32 all64

pre:
	mkdir -p $(OUTDIR)/mingw32/bin
	mkdir -p $(OUTDIR)/mingw32/lib
	mkdir -p $(OUTDIR)/mingw32/include
	mkdir -p $(OUTDIR)/mingw64/bin
	mkdir -p $(OUTDIR)/mingw64/lib
	mkdir -p $(OUTDIR)/mingw64/include


CFLAGS=-O2 -DTARGET_HOST_MS_WINDOWS -DFREEGLUT_EXPORTS -I. -I$(FREEGLUT)/include
LDFLAGS=-lopengl32 -lgdi32 -lwinmm
 
$(objs32): %.32.o: %.c
	$(TOOL32)-gcc -m32 -c $(CFLAGS) $< -o $@
 
$(objs64): %.64.o: %.c
	$(TOOL64)-gcc -m64 -c $(CFLAGS) $< -o $@

dll32: $(objs32) pre
	$(TOOL32)-gcc -m32 -shared $(objs32) $(LDFLAGS) -o $(OUTDIR)/mingw32/bin/freeglut.dll -Wl,--out-implib,$(OUTDIR)/mingw32/lib/libfreeglut-gen.dll.a
      
dll64: $(objs64) pre
	$(TOOL64)-gcc -m64 -shared $(objs64) $(LDFLAGS) -o $(OUTDIR)/mingw64/bin/freeglut.dll -Wl,--out-implib,$(OUTDIR)/mingw64/lib/libfreeglut-gen.dll.a
   
importlib32: dll32 pre
	$(TOOL32)-dlltool --dllname $(OUTDIR)/mingw32/bin/freeglut.dll --input-def $(DEFFILE) --output-lib $(OUTDIR)/mingw32/lib/libfreeglut.dll.a
  
importlib64: dll64 pre
	$(TOOL64)-dlltool --dllname $(OUTDIR)/mingw64/bin/freeglut.dll --input-def $(DEFFILE) --output-lib $(OUTDIR)/mingw64/lib/libfreeglut.dll.a
	
all32: dll32 importlib32 copy-include32
all64: dll64 importlib64 copy-include64
	
copy-include32:
	cp -ar $(FREEGLUT)/include/GL $(OUTDIR)/mingw32/include/

copy-include64:
	cp -ar $(FREEGLUT)/include/GL $(OUTDIR)/mingw64/include/


