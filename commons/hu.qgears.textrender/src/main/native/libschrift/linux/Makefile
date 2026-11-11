JNI_ARCHPOSTFIX:=`uname -m | sed -e 's/x86_64/amd64/' -e 's/x86/i386/' -e 's/i686/i386/'`
JNI_INCLUDE:=-I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.7.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.8.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/java-1.11.0-openjdk-${JNI_ARCHPOSTFIX}/include \
    -I/usr/lib/jvm/java-1.11.0-openjdk-${JNI_ARCHPOSTFIX}/include/linux \
    -I/usr/lib/jvm/default-java/include \
    -I/usr/lib/jvm/default-java/include/linux
ARCHPOSTFIX:=$(shell uname -m | sed -e 's/\bx86\b/i686/')
SO_NAME:=libqlibschrift-${ARCHPOSTFIX}.so
SO_SOFT_FLOAT_NAME:=libqlibschrift_sf-${ARCHPOSTFIX}.so

# highest glibc symbol version allowed in the x86 libs (Debian 9)
GLIBC_MAX ?= 2.24

#used by the maven editor_platform_build process, see pom.xml in project.
PROJDIR= ../../../../..

ifndef OUTPUTDIR
    OUTPUTDIR = $(PROJDIR)/src/main/resources/hu/qgears/textrender/libschrift
endif


JNIDEPS=$(PROJDIR)/target/classes:$(PROJDIR)/../hu.qgears.images/target/classes:$(PROJDIR)/../hu.qgears.commons/target/classes:$(PROJDIR)/../hu.qgears.nativeloader/target/classes


ifeq ($(RELEASE),1)
#for release builds is the default
    CONDITIONAL_FLAGS = -O2 
else
#debug is the default
    CONDITIONAL_FLAGS = -g -O0 -DDEBUG=1
endif

# Flags making hardware float arithmetic bit-exact across i386/x86_64/arm64
# Linux and macOS, independently of the optimization level:
#  -ffp-contract=off  : never fuse a*b+c into FMA (single rounding gives
#                       different results; arm64 and clang fuse by default,
#                       x86_64 does too with -march supporting FMA).
#  -fno-fast-math     : forbid value-changing optimizations (reassociation,
#                       reciprocal division, flush-to-zero, finite-math).
#  -fexcess-precision=standard : round to the declared type on every
#                       assignment and cast (only matters with x87).
#  -msse2 -mfpmath=sse (i386 only): use SSE instead of the x87 FPU, whose
#                       80-bit intermediates make results depend on register
#                       spilling; gives FLT_EVAL_METHOD == 0 like x86_64/arm64.
# Do not add -march=native, -Ofast, -ffast-math or -funsafe-math-optimizations.
DETERMINISTIC_FP_FLAGS= \
	-ffp-contract=off \
	-fno-fast-math \
	-fexcess-precision=standard

ifneq ($(filter i386 i486 i586 i686,$(ARCHPOSTFIX)),)
    DETERMINISTIC_FP_FLAGS += -msse2 -mfpmath=sse
endif

C_FLAGS=-fPIC -D_REENTRANT \
	$(CONDITIONAL_FLAGS) -fvisibility=hidden -std=c99 -pedantic -Wconversion -Wshadow  \
	$(DETERMINISTIC_FP_FLAGS) \
	${JNI_INCLUDE_PLATFORM} ${JNI_INCLUDE}

SOFT_FLOAT_FLAGS=-DSOFTFLOAT=1

# fail at link time on unresolved symbols (e.g. a leftover libm call)
LIB_LDFLAGS=-Wl,-z,defs

# sources shared by the shared library and the standalone manual-test programs
SOURCES=../nativeLibLibschrift.c ../schrift.c ../util.c

JNI_HEADERS=../hu_qgears_textrender_libschrift_LibschriftNative.h ../hu_qgears_textrender_TrueTypeFont.h

SOURCES_LIB=$(SOURCES) ../nativeLibLibschriftJni.c


.DEFAULT_GOAL := all
.PHONY: all lib lib_original checkdeps bversion testprog asms clean softfloatlib restore_soft restore_normal restore restore_float32

all : lib softfloatlib bversion testprog

lib : $(JNI_HEADERS) $(SOURCES_LIB)
	mkdir -p $(OUTPUTDIR)
	gcc -o $(OUTPUTDIR)/$(SO_NAME) \
	$(C_FLAGS) -shared \
	-Wl,-soname,$(SO_NAME) \
	$(SOURCES_LIB) \
	$(LIB_LDFLAGS)

# optional: original double based code, depends on libm; overwrites the lib output
lib_original : $(JNI_HEADERS) $(SOURCES_LIB)
	mkdir -p $(OUTPUTDIR)
	gcc -o $(OUTPUTDIR)/$(SO_NAME) \
	-DUSE_DOUBLE=1 \
	$(C_FLAGS) -shared \
	-Wl,-soname,$(SO_NAME) \
	$(SOURCES_LIB) \
	$(LIB_LDFLAGS) \
	-lm

# optional: verify the x86 libs in OUTPUTDIR load on glibc $(GLIBC_MAX)
CHECKDEPS_LIBS=libqlibschrift-i686.so libqlibschrift_sf-i686.so \
	libqlibschrift-x86_64.so libqlibschrift_sf-x86_64.so

checkdeps :
	@rc=0; \
	for n in $(CHECKDEPS_LIBS); do \
		f=$(OUTPUTDIR)/$$n; \
		[ -f $$f ] || continue; \
		max=$$(objdump -T $$f | grep -o 'GLIBC_[0-9.]*' | sed 's/GLIBC_//' | sort -Vu | tail -n1); \
		if [ "$$(printf '%s\n%s\n' "$$max" "$(GLIBC_MAX)" | sort -V | tail -n1)" != "$(GLIBC_MAX)" ]; then \
			echo "FAIL $$f requires GLIBC_$$max > GLIBC_$(GLIBC_MAX):"; \
			objdump -T $$f | grep "GLIBC_$$max"; \
			rc=1; \
		else \
			echo "OK   $$f (max GLIBC_$$max)"; \
		fi; \
	done; \
	exit $$rc

softfloatlib : $(JNI_HEADERS) $(SOURCES_LIB)
	mkdir -p $(OUTPUTDIR)
	gcc -o $(OUTPUTDIR)/$(SO_SOFT_FLOAT_NAME) \
	$(SOFT_FLOAT_FLAGS) \
	$(C_FLAGS)  -shared \
	-Wl,-soname,$(SO_SOFT_FLOAT_NAME) \
	$(SOURCES_LIB) \
	$(LIB_LDFLAGS)

bversion:
	gcc $(SOFT_FLOAT_FLAGS) \
	-DINCLUDE_MAIN=1 \
	-o libschrift_manualtest_softfloat.exe \
	$(C_FLAGS) \
	$(SOURCES)

testprog:
	gcc \
	-DINCLUDE_MAIN=1 \
	-o libschrift_manualtest.exe \
	$(C_FLAGS) \
	$(SOURCES)

../hu_qgears_textrender_libschrift_LibschriftNative.h : 
	javac -cp $(JNIDEPS) \
	-h .. $(PROJDIR)/src/main/java/hu/qgears/textrender/libschrift/LibschriftNative.java 

../hu_qgears_textrender_TrueTypeFont.h :
	javac -cp $(JNIDEPS) -h .. $(PROJDIR)/src/main/java/hu/qgears/textrender/TrueTypeFont.java

%.s : ../%.c 
	gcc $(C_FLAGS) -fverbose-asm -S -o $@ $<


asms : $(notdir $(SOURCES_LIB:.c=.s))
	@echo "Create " $<

restore_soft : $(SOURCES_LIB)
	mkdir -p restored_soft
	@for f in $(SOURCES_LIB); do \
		perl expand_macros.pl ../float_macros_softfloat.h $$f > restored_soft/$$(basename $$f) || exit 1; \
	done

restore_normal : $(SOURCES_LIB)
	mkdir -p restored_normal
	@for f in $(SOURCES_LIB); do \
		perl expand_macros.pl ../float_macros_original.h $$f > restored_normal/$$(basename $$f) || exit 1; \
	done

restore_float32 : $(SOURCES_LIB)
	mkdir -p restore_float32
	@for f in $(SOURCES_LIB); do \
		perl expand_macros.pl ../float_macros_original_32.h $$f > restore_float32/$$(basename $$f) || exit 1; \
	done

restore : restore_normal restore_soft restore_float32

clean:
	@rm -fv $(OUTPUTDIR)/$(SO_NAME)
	@rm -fv $(OUTPUTDIR)/$(SO_SOFT_FLOAT_NAME)
	@rm -fv *.s
	@rm -fv *.exe
	@rm -fv restored_normal/* restored_soft/*


