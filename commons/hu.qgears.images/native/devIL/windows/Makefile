JNI_INCLUDE = -I/usr/lib/jvm/java-6-openjdk/include -I./jni-win32
#JNI_INCLUDE = 

OUTDIR = ../../../src/hu/qgears//images/devil
CPP_WIN_FLAGS += -Wl,--add-stdcall-alias -D_REENTRANT -shared
CROSSCPP=i586-mingw32msvc-g++ 
WIN_DEPS = /home/rizsi/rizsi-git/win-deps
DEVIL = ${WIN_DEPS}/mingw/devil

WIN_INCLUDES = -I${DEVIL}/include
WIN_LIBS = ${DEVIL}/DevIL.lib

gnu_c:
	${CROSSCPP} -o ${OUTDIR}/rdevil.dll \
	-shared \
	${WIN_INCLUDES} \
	${JNI_INCLUDE} \
	../nativeDevil.cpp ../jniutil.cpp \
	${WIN_LIBS} \
	
	cp ${DEVIL}/DevIL.dll ${OUTDIR}
	