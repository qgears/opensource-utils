#include<stdio.h>
#include<stdlib.h>
#include "malloc.h"

#include "hu_qgears_shm_dlmalloc_DlMallocPoolNative.h"
#include "jniutil.h"
typedef struct
{
	mspace space;
	void * base;
	jlong size;
} rdlmalloc;
#define CLASS Java_hu_qgears_shm_dlmalloc_DlMallocPoolNative_
#define JNISTRUCT rdlmalloc
#define EXCCLASS "hu/qgears/shm/dlmalloc/DlMallocException"
#define EXCCLASS_OUT "hu/qgears/shm/dlmalloc/DlMallocExceptionOutOfMemory"
static jfieldID FID;

METHODPREFIX(CLASS, void, init)(ST_ARGS, jobject byteBuffer, jlong size, jboolean synchronize)
{
	void * base=env->GetDirectBufferAddress(byteBuffer);
	mspace space=create_mspace_with_base(base, size, synchronize?1:0);
	if(space==NULL)
	{
		JNU_ThrowByName(env, EXCCLASS, "Error creating mspace in buffer (buffer size too small?)");
	}
	initObj(env, obj, "ptr", sizeof(JNISTRUCT));
	FID=getLongFieldId(env, obj, "ptr");
	MYHEADID(JNISTRUCT, FID);
	str->space=space;
	str->base=base;
	str->size=size;
}
METHODPREFIX(CLASS, jobject, dlalloc)(ST_ARGS, jlong size, jint align)
{
	MYHEADID(JNISTRUCT, FID);
	void * ret=mspace_malloc(str->space, size);
	char * base=(char *)(str->base);
	if(ret<str->base || ret>=base+str->size)
	{
		// mspace is filled and implementation backed to stdc malloc
		// Our specification does not let this happen
		mspace_free(str->space, ret);
		JNU_ThrowByName(env, EXCCLASS_OUT, "Out of mspace area");
		return NULL;
	}
	memset ( ret, 0, size );
	return env->NewDirectByteBuffer(ret, size);
}
METHODPREFIX(CLASS, void, dlfree)(ST_ARGS, jobject byteBuffer)
{
	MYHEADID(JNISTRUCT, FID);
	void * ptr=env->GetDirectBufferAddress(byteBuffer);
	mspace_free(str->space, ptr);
}
METHODPREFIX(CLASS, jlong, getRelativeAddress)(ST_ARGS, jobject byteBuffer)
{
	MYHEADID(JNISTRUCT, FID);
	char * ptr=(char *)env->GetDirectBufferAddress(byteBuffer);
	char * base = (char *)str->base;
	return ptr-base;
}
METHODPREFIX(CLASS, void, nativeDispose)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	disposeObj(env, obj, "ptr");
}
METHODPREFIX(CLASS, jlong, getNativePointer)(ST_ARGS, jobject byteBuffer, jint lsbOrFsb)
{
	MYHEADID(JNISTRUCT, FID);
	void * ptr=env->GetDirectBufferAddress(byteBuffer);
	if(lsbOrFsb==2)
	{
		return 0;
	}
	return (jlong) ptr;
}
/**
struct mallinfo {
  MALLINFO_FIELD_TYPE arena;    /* non-mmapped space allocated from system
  MALLINFO_FIELD_TYPE ordblks;  /* number of free chunks
  MALLINFO_FIELD_TYPE smblks;   /* always 0 
  MALLINFO_FIELD_TYPE hblks;    /* always 0
  MALLINFO_FIELD_TYPE hblkhd;   /* space in mmapped regions
  MALLINFO_FIELD_TYPE usmblks;  /* maximum total allocated space 
  MALLINFO_FIELD_TYPE fsmblks;  /* always 0 
  MALLINFO_FIELD_TYPE uordblks; /* total allocated space 
  MALLINFO_FIELD_TYPE fordblks; /* total free space 
  MALLINFO_FIELD_TYPE keepcost; /* releasable (via malloc_trim) space 
};
*/
METHODPREFIX(CLASS, jlong, getAllocatedSize)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	struct mallinfo info=mspace_mallinfo(str->space);
	return info.uordblks;
}
METHODPREFIX(CLASS, jlong, getMaxAllocated)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	struct mallinfo info=mspace_mallinfo(str->space);
	return info.usmblks;
}

