#include<stdio.h>
#include<stdlib.h>

#include "hu_qgears_shm_part_PartNativeMemoryNative.h"
#include "jniutil.h"
#define CLASS Java_hu_qgears_shm_part_PartNativeMemoryNative_

METHODPREFIX(CLASS, jobject, getByteBuffer)(ST_ARGS, jobject byteBuffer, jlong offset, jlong size)
{
	char * base=(char *)env->GetDirectBufferAddress(byteBuffer);
	base+=offset;
	return env->NewDirectByteBuffer(base, size);
}
METHODPREFIX(CLASS, jlong, getNativePointer)(ST_ARGS, jobject byteBuffer, jint lsbOrFsb)
{
	void * ptr=env->GetDirectBufferAddress(byteBuffer);
	if(lsbOrFsb==2)
	{
		return 0;
	}
	return (jlong) ptr;
}

METHODPREFIX(CLASS, jobject, getBuffer)(ST_ARGS, 
	jlong ptr1, jlong ptr2, jlong size)
{
	return env->NewDirectByteBuffer((void *)ptr1, size);
}
