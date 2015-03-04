#include <windows.h>
#include <stdio.h>

#include "../hu_qgears_shm_SharedMemoryNative.h"
#include "../jniutil.h"
#define MAX_SHM_NAME_LENGTH 255
typedef struct
{
	HANDLE hMapFile;
	jlong size;
	void * ptr;
	char shmName[MAX_SHM_NAME_LENGTH];
} rshm;
#define CLASS Java_hu_qgears_shm_SharedMemoryNative_
#define JNISTRUCT rshm
#define EXCCLASS "hu/qgears/shm/SharedMemoryException"

static jfieldID FID;

METHODPREFIX(CLASS, void, init)(ST_ARGS, jstring id, jint createTypeIn, jlong size)
{
	char shmName[MAX_SHM_NAME_LENGTH];
	if(copyStringInput(env, obj, id, shmName,
		MAX_SHM_NAME_LENGTH, EXCCLASS, "shared mem id too long"))
			return;

	int create=createTypeIn==0||createTypeIn==2;
	if(createTypeIn==3)
	{
		return;
	}
	// TODO in case 0 throw exception if exists!
	HANDLE hMapFile;
	if(create)
	{
	  hMapFile = CreateFileMapping(
                 INVALID_HANDLE_VALUE,	// use paging file
                 NULL,	// default security 
                 PAGE_READWRITE,	// read/write access
                 0,	// maximum object size (high-order DWORD) 
                 size,	// maximum object size
														// (low-order DWORD)
				shmName);	// name of mapping object
	}
	else
	{
		hMapFile = OpenFileMapping(
			FILE_MAP_READ|FILE_MAP_WRITE,   // read/write access
			FALSE, // do not inherit the name
			shmName);    // name of mapping object 
	}
	if(hMapFile<0)
	{
		int err=GetLastError();
		JNU_ThrowByNameErrno(env, EXCCLASS, "creating shared memory: ", err);
		return;
	}
	void * ptr= MapViewOfFile(
		hMapFile,   // handle to map object
		FILE_MAP_READ|FILE_MAP_WRITE, // read/write permission
		0,
		0,
		size);
	if(ptr==NULL)
	{
		int err=GetLastError();
		JNU_ThrowByNameErrno(env, EXCCLASS, "Mapping to memory address", err);
		CloseHandle(hMapFile);
		return;
	}
	MEMORY_BASIC_INFORMATION info;
	if(VirtualQuery(ptr, &info, sizeof(info))==0)
	{
		int err=GetLastError();
		JNU_ThrowByNameErrno(env, EXCCLASS, "Querying mapped memory size: ", err);
		return;
	}
	size=info.RegionSize;
	initObj(env, obj, "ptr", sizeof(JNISTRUCT));
	FID=getLongFieldId(env, obj, "ptr");
	MYHEADID(JNISTRUCT, FID);
	str->hMapFile=hMapFile;
	str->size=size;
	str->ptr=ptr;
	strcpy(str->shmName, shmName);
}
METHODPREFIX(CLASS, void, sync)(ST_ARGS, jboolean write)
{
	MYHEADID(JNISTRUCT, FID);
	// Sync of shared mem is not required
}
METHODPREFIX(CLASS, void, nativeDispose)(ST_ARGS, jboolean unlink)
{
	MYHEADID(JNISTRUCT, FID);
	UnmapViewOfFile(str->ptr);
	CloseHandle(str->hMapFile);
	//	if(unlink) - On windows shared memory is disposed with last client
	disposeObj(env, obj, "ptr");
}

METHODPREFIX(CLASS, jlong, getSize)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	return str->size;
}
METHODPREFIX(CLASS, jobject, getAccessor)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	return env->NewDirectByteBuffer(str->ptr, str->size);
}
METHODPREFIX(CLASS, jlong, getNativePointer1)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	return (jlong)(str->ptr);
}
METHODPREFIX(CLASS, jlong, getNativePointer2)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	return 0;
}

