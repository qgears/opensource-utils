#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
//#include <asm/cachectl.h>

#include "../hu_qgears_shm_SharedMemoryNative.h"
#include "../jniutil.h"
#define MAX_SHM_NAME_LENGTH 255
typedef struct
{
	int shmemid;
	jlong size;
	void * ptr;
	char shmName[MAX_SHM_NAME_LENGTH];
	jboolean hasNumberId;
} rshm;
#define CLASS Java_hu_qgears_shm_SharedMemoryNative_
#define JNISTRUCT rshm
#define EXCCLASS "hu/qgears/shm/SharedMemoryException"

static jfieldID FID;

METHODPREFIX(CLASS, void, init2)(ST_ARGS, jlong shmid)
{
	struct shmid_ds buf;
	int ret=shmctl(shmid, IPC_STAT, &buf);

	if(ret<0)
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "shmctl", errno);
	}
	jlong size=buf.shm_segsz;
  char * ptr = (char *)shmat(shmid, 0, 0);
	if(ptr==(void *)-1)
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "shmat", errno);
	}

	initObj(env, obj, "ptr", sizeof(JNISTRUCT));
	FID=getLongFieldId(env, obj, "ptr");
	MYHEADID(JNISTRUCT, FID);
	str->shmemid=shmid;
	str->size=size;
	str->ptr=ptr;
	str->hasNumberId=1;
	strcpy(str->shmName, "");
}
METHODPREFIX(CLASS, void, deleteSharedMemoryById)(ST_ARGS, jlong shmid)
{
	int ret=shmctl(shmid, IPC_RMID, NULL);
	if(ret<0)
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "shmctl", errno);
	}
}

METHODPREFIX(CLASS, void, init)(ST_ARGS, jstring id, jint createTypeIn, jlong size)
{
	struct stat buf;
	char shmName[MAX_SHM_NAME_LENGTH];
	if(copyStringInput(env, obj, id, shmName, MAX_SHM_NAME_LENGTH, EXCCLASS, "shared mem id too long")) return;
	int createType=O_RDWR;
	int createMode=S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP;
	if(createTypeIn==2)
	{
		shm_unlink(shmName);
	}
	switch(createTypeIn)
	{
		// createFailsIfExists
		case 0:
		case 2:
			createType|=O_CREAT|O_EXCL;
			break;
		// use
		case 1:
			break;
	}
	int shmemid=shm_open(shmName, createType, createMode);
	if(shmemid<0)
	{
		int err=errno;
		JNU_ThrowByNameErrno(env, EXCCLASS, "creating shared memory: ", err);
		return;
	}
	int ret;
	switch(createTypeIn)
	{
		// createFailsIfExists
		case 0:
		case 2:
			ret=ftruncate(shmemid, size);
			if(ret<0)
			{
				JNU_ThrowByNameErrno(env, EXCCLASS, "setting shared memory size: ", errno);
				close(shmemid);
				return;
			}
			break;
		// use
		case 1:
			{
				if(fstat(shmemid, &buf))
				{
					int err=errno;
					JNU_ThrowByNameErrno(env, EXCCLASS, "fstat shm (mapping file into memory): ", err);
					return;
				}
				size=buf.st_size;
 			}
			break;
	}
	void * ptr=mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, shmemid, 0);
	if (ptr == NULL)
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "mmap shared memory: ", errno);
		close(shmemid);
		return;
	}
	initObj(env, obj, "ptr", sizeof(JNISTRUCT));
	FID=getLongFieldId(env, obj, "ptr");
	MYHEADID(JNISTRUCT, FID);
	str->shmemid=shmemid;
	str->size=size;
	str->ptr=ptr;
	str->hasNumberId=0;
	strcpy(str->shmName, shmName);
}
METHODPREFIX(CLASS, void, initFile)(ST_ARGS, jstring fileNameJ)
{
	struct stat buf;
	char fileName[MAX_SHM_NAME_LENGTH];
	if(copyStringInput(env, obj, fileNameJ, fileName, MAX_SHM_NAME_LENGTH, EXCCLASS, "file name too long")) return;
	int createType=O_RDWR;
	int createMode=S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP;
	int fd=open(fileName, O_RDWR);
	if(fd<0)
	{
		int err=errno;
		JNU_ThrowByNameErrno(env, EXCCLASS, "mapping file into memory: ", err);
		return;
	}
	if(fstat(fd, &buf))
	{
		int err=errno;
		JNU_ThrowByNameErrno(env, EXCCLASS, "fstat file (mapping file into memory): ", err);
		return;
	}
	jlong size=buf.st_size;
	void * ptr=mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
	if (ptr == NULL)
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "mmap file memory: ", errno);
		close(fd);
		return;
	}
	initObj(env, obj, "ptr", sizeof(JNISTRUCT));
	FID=getLongFieldId(env, obj, "ptr");
	MYHEADID(JNISTRUCT, FID);
	str->shmemid=fd;
	str->size=size;
	str->ptr=ptr;
	str->hasNumberId=0;
	strcpy(str->shmName, fileName);
}
METHODPREFIX(CLASS, void, sync)(ST_ARGS, jboolean write)
{
	MYHEADID(JNISTRUCT, FID);
//	msync(str->ptr, str->size, MS_SYNC);
//	cacheflush(str->ptr, str->size, DCACHE);
	if(write)
	{
		msync(str->ptr, str->size, MS_SYNC);
	}
	else
	{
		msync(str->ptr, str->size, MS_INVALIDATE);
	}
}
METHODPREFIX(CLASS, void, nativeDispose)(ST_ARGS, jboolean unlink)
{
	MYHEADID(JNISTRUCT, FID);
	if(str->hasNumberId)
	{
		shmdt(str->ptr);
	}else
	{
		munmap(str->ptr, str->size);
		close(str->shmemid);
		if(unlink)
		{
			shm_unlink(str->shmName);
		}
	}
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

