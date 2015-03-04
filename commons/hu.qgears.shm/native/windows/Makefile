# Windows Make file for shared memory and named semaphor wrapper JNI library
JNI_INCLUDE = -I/usr/lib/jvm/java-6-openjdk/include -I./jni-win32 \
	-I/usr/lib/jvm/java-6-openjdk-amd64/include
#JNI_INCLUDE =
CPP_WIN_FLAGS += -Wl,--add-stdcall-alias -D_REENTRANT -shared
CROSSCPP=i586-mingw32msvc-g++ 

gnu_c:
	${CROSSCPP} -o ../../src/hu/qgears/shm/natives/shm.dll \
	${CPP_WIN_FLAGS} \
	${JNI_INCLUDE} ../malloc.c \
	 ../jniutil.cpp ../DlMallocPoolNative.cpp \
	 ../PartNativeMemoryNative.cpp SemaphoreNative.cpp \
	SharedMemoryNative.cpp
