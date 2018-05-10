#
#
#	shm.dll makefile for msys2
#
#

JDKPATH = /C/Program\ Files/Java/jdk1.8.0_60/

JNI_INCLUDE= \
-I$(JDKPATH)/include  \
-I$(JDKPATH)/include/win32 \


gnu_c:
	gcc -m64 -o ../../src/hu/qgears/shm/natives/shm.dll \
	-D_REENTRANT -shared \
	${JNI_INCLUDE} \
	../msys_malloc.c ../jniutil.cpp ../DlMallocPoolNative.cpp ../PartNativeMemoryNative.cpp \
	SemaphoreNative.cpp SharedMemoryNative.cpp \
	-static-libgcc -Wl,-Bstatic -lgcc -lstdc++
