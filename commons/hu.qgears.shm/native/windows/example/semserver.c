#include <windows.h>
#include <stdio.h>
#include <conio.h>
#include <tchar.h>

#define BUF_SIZE 256
TCHAR szName1[]=TEXT("rgl1");
TCHAR szName2[]=TEXT("rgl2");
TCHAR szMsg[]=TEXT("Message from first process.");


int _tmain()
{
	HANDLE hSem1;
	HANDLE hSem2;
	hSem1=CreateSemaphore(
	  NULL, //__in_opt  LPSECURITY_ATTRIBUTES lpSemaphoreAttributes,
	  0, //__in      LONG lInitialCount,
	  1024, //__in      LONG lMaximumCount,
	  szName1 // __in_opt  LPCTSTR lpName
	);
	hSem2=CreateSemaphore(
	  NULL, //__in_opt  LPSECURITY_ATTRIBUTES lpSemaphoreAttributes,
	  0, //__in      LONG lInitialCount,
	  1024, //__in      LONG lMaximumCount,
	  szName2 // __in_opt  LPCTSTR lpName
	);
	ReleaseSemaphore(
		hSem1,	//  __in       HANDLE hSemaphore,
		1, //  __in       LONG lReleaseCount,
		NULL // __out_opt  LPLONG lpPreviousCount
	);
	printf("Waiting for client!\n");
	fflush(stdout);
	WaitForSingleObject(hSem2, INFINITE);
	printf("Client arrived!\n");
	fflush(stdout);
	return 0;
}

