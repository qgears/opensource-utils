// Named Sempahore Windows implementation

// delete semaphore has no use in Windows
// Windows semantics delete semaphore when last handle (process) is closed

#include <windows.h>
#include <stdio.h>

#include "../hu_qgears_shm_sem_SemaphoreNative.h"
#include "../jniutil.h"
#define MAX_SHM_NAME_LENGTH 255
typedef struct
{
	HANDLE hSem;
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
//	int createType=0;
//	int createMode=S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP;
	int create=createTypeIn==0||createTypeIn==2;
	if(createTypeIn==3)
	{
		return;
	}
	// TODO in case 0 throw exception if exists!
//	printf("name: %s, createtype: %d %d\n", semName, createType, createMode);
	HANDLE hSem;
	if(create)
	{
		hSem=CreateSemaphore(
	  	NULL, //__in_opt  LPSECURITY_ATTRIBUTES lpSemaphoreAttributes,
	  	0, //__in      LONG lInitialCount,
	  	1024, //__in      LONG lMaximumCount,
	  	semName // __in_opt  LPCTSTR lpName
		);
	}
	else
	{
		hSem=OpenSemaphore(
			SEMAPHORE_ALL_ACCESS,
//		  SEMAPHORE_MODIFY_STATE,
		  FALSE,
		  semName);
	}
	if(hSem==NULL)
	{
		int err=GetLastError();
		JNU_ThrowByNameErrno(env, EXCCLASS, "creating semaphore: ", err);
		return;
	}
	initObj(env, obj, "ptr", sizeof(JNISTRUCT));
	FID=getLongFieldId(env, obj, "ptr");
	MYHEADID(JNISTRUCT, FID);
	str->hSem=hSem;
	strcpy(str->semName, semName);
}
METHODPREFIX(CLASS, void, nativeDispose)(ST_ARGS, jboolean unlink)
{
	MYHEADID(JNISTRUCT, FID);
	CloseHandle(str->hSem);
	// unlink has no use on Windows that frees objects when no
	// handle remains
	disposeObj(env, obj, "ptr");
}
METHODPREFIX(CLASS, void, incrementValue)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	if(!ReleaseSemaphore(str->hSem, 1, NULL))
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "posting semaphore: ",
			GetLastError());
	}
}
int waitForObjectAndCheckRet(ST_ARGS, HANDLE hSem, DWORD timeout)
{
//	MYHEADID(JNISTRUCT, FID);
	int ret=WaitForSingleObject(hSem, timeout);
	if(ret==WAIT_ABANDONED)
	{
		JNU_ThrowByName(env, EXCCLASS, "waiting semaphore: semaphore is abandoned");
	}
	if(ret==WAIT_FAILED)
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "waiting semaphore: ",
			GetLastError());
	}
	return ret;
}
METHODPREFIX(CLASS, void, decrementValue)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	waitForObjectAndCheckRet(env, obj, str->hSem, INFINITE);
}
METHODPREFIX(CLASS, jboolean, decrementValueTry)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	int ret=waitForObjectAndCheckRet(env, obj, str->hSem, 0);
//	int ret=sem_trywait(str->semid);
	if(ret==WAIT_TIMEOUT)
	{
		return 0;
	}
	return 1;
}
METHODPREFIX(CLASS, jboolean, decrementValueTimed)(ST_ARGS, jlong timeoutTimeMillis, jlong timeoutTimeMillisAbsolute)
{
	MYHEADID(JNISTRUCT, FID);
	int ret=waitForObjectAndCheckRet(env, obj, str->hSem, timeoutTimeMillis);
	if(ret==WAIT_TIMEOUT)
	{
		return 0;
	}
	return 1;
}
METHODPREFIX(CLASS, jint, getValue)(ST_ARGS)
{
	MYHEADID(JNISTRUCT, FID);
	LONG prevValue=0;
	if(!ReleaseSemaphore(str->hSem, 0, &prevValue))
	{
		JNU_ThrowByNameErrno(env, EXCCLASS, "posting semaphore: ",
			GetLastError());
	}
	return (jint)prevValue;
}

