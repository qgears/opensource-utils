JDKPATH = /C/Program\ Files/Java/jdk1.8.0_60/

JNI_INCLUDE= \
-I$(JDKPATH)/include  \
-I$(JDKPATH)/include/win32 \

OUTDIR = ../../../src/hu/qgears//images/devil
CPP_WIN_FLAGS += -Wl,--add-stdcall-alias -D_REENTRANT -shared

DEVIL_INCLUDE = -I./devil-msys/include
DEVIL_LIB = -L./devil-msys/lib/x64

DLLS = 

gnu_c:
	gcc -m64 -o ${OUTDIR}/rdevil.dll \
	-shared \
	${JNI_INCLUDE} \
	${DEVIL_INCLUDE} \
	${DEVIL_LIB} \
	../nativeDevil.cpp ../jniutil.cpp \
	-lIL
	
#	
#	cp ./devil/lib/x64/cygIL.dll ${OUTDIR}
#	cp $(addprefix C:/cygwin64/bin/, $(DLLS)) $(OUTDIR)/
#	