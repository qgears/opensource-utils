#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <semaphore.h>

#include "../hu_qgears_shm_sem_SemaphoreNative.h"
#include "../jniutil.h"
#define MAX_SHM_NAME_LENGTH 255
typedef struct
{
	sem_t * semid;
	char semName[MAX_SHM_NAME_LENGTH];
} rsem;
#define CLASS Java_hu_qgears_shm_sem_SemaphoreNative_
#define JNISTRUCT rsem
#define EXCCLASS "hu/qgears/shm/sem/SemaphoreException"

static jfieldID FID;

METHODPREFIX(CLASS, void, init)(ST_ARGS, jstring id, jint createTypeIn)
{
	char semName[MAX_SHM_NAME_LENGTH];
	if(copyStringInput(env, obj, id, semName, MAX_SHM_NAME_LENGTH, EXCCLASS, "semaphore id too long")) return;
	int createType=0;
	int createMode=S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP;
	if(createTypeIn==2)
	{
		sem_unlink(semName);
	}
	if(createTypeIn==3)
	{
		sem_unlink(semName);
		return;
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
//	printf("name: %s, createtype: %d %d\n", semName, createType, createMode);
	sem_t * semid=sem_open(semName, createType, createMode, 0);
	if(semid==SEM_FAILED)
	{
		int err=errno;
		JNU_ThrowByNameErrno(env, EXCCLASS, "creating semaphore: ", err);
		return;
	}
	initObj(env, obj, "ptr", sizeof(JNISTRUCT));
	FID=getLongFieldId(env, obj, "ptr");
	MYHEADID(JNISTRUCT, FID);
	str->semid=semid;
	strcpy(str->semName, semName);
}
METHODPREFIX(CLASS, void, nativeDispose)(ST_ARGS, jboolean unlink)
{
	MYHEADID(JNISTRUCT, FID);
	sem_close(str->semid);
	if(unlink)
	{
		sem_unlink(str->semName);
	}
	disposeObj(env, obj, "ptr");
}
METHODPREFIX(CLASS, void, incrementValue)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	if(sem_post(str->semid))
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "posting semaphore: ", errno);
	}
}
METHODPREFIX(CLASS, void, decrementValue)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	if(sem_wait(str->semid))
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "waiting semaphore: ", errno);
	}
}
METHODPREFIX(CLASS, jboolean, decrementValueTry)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	int ret=sem_trywait(str->semid);
	if(ret)
	{
		ret=errno;
		if(ret==EAGAIN)
		{
			return 0;
		}
		else
		{
			JNU_ThrowByNameErrno(env, EXCCLASS, "try waiting semaphore: ", ret);
			return 0;
		}
	}
	return 1;
}
METHODPREFIX(CLASS, jboolean, decrementValueTimed)(ST_ARGS, jlong timeoutTimeMillisRelative, jlong timeoutTimeMillis)
{
	MYHEADID(JNISTRUCT, FID);
	struct timespec ts;
	ts.tv_sec=timeoutTimeMillis/1000;
	ts.tv_nsec=(timeoutTimeMillis%1000)*1000*1000;
//               time_t tv_sec;      /* Seconds */
//              long   tv_nsec;     /* Nanoseconds [0 .. 999999999] */
	int ret=sem_timedwait(str->semid, &ts);
	if(ret)
	{
		ret=errno;
		if(ret==ETIMEDOUT)
		{
			return 0;
		}
		else
		{
			JNU_ThrowByNameErrno(env, EXCCLASS, "timed waiting semaphore: ", ret);
			return 0;
		}
	}
	return 1;
}
METHODPREFIX(CLASS, jint, getValue)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	int ret;
	sem_getvalue(str->semid, &ret);
	return ret;
}

//METHODPREFIX(CLASS, void, 
