JNI_INCLUDE = -IC:/cygwin64/usr/jdkinclude -I./jni-win32
#JNI_INCLUDE = 

OUTDIR = ../../../src/hu/qgears//images/devil
CPP_WIN_FLAGS += -Wl,--add-stdcall-alias -D_REENTRANT -shared

DEVIL_INCLUDE = -I./devil/include
DEVIL_LIB = -L./devil/lib/x64

DLLS = CYGGCC_S-SEH-1.DLL CYGJASPER-4.DLL CYGJBIG-2.DLL CYGJPEG-8.DLL CYGLZMA-5.DLL CYGPNG16-16.DLL CYGSTDC++-6.DLL CYGTIFF-6.DLL CYGWIN1.DLL CYGZ.DLL

gnu_c:
	gcc -m64 -o ${OUTDIR}/rdevil.dll \
	-shared \
	-DM_PI='(3.14159265358979323846264338327950288419716939937510)' -D__int64=int64_t \
	${JNI_INCLUDE} \
	${DEVIL_INCLUDE} \
	${DEVIL_LIB} \
	../nativeDevil.cpp ../jniutil.cpp \
	-lIL
	
	cp ./devil/lib/x64/cygIL.dll ${OUTDIR}
	cp $(addprefix C:/cygwin64/bin/, $(DLLS)) $(OUTDIR)/
	