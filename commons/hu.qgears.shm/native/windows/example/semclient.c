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
	hSem1=OpenSemaphore(
	  SEMAPHORE_MODIFY_STATE, // __in  DWORD dwDesiredAccess,
	  FALSE, //__in  BOOL bInheritHandle,
	  szName1 // __in_opt  LPCTSTR lpName
	);
	hSem2=OpenSemaphore(
	  SEMAPHORE_MODIFY_STATE, // __in  DWORD dwDesiredAccess,
	  FALSE, //__in  BOOL bInheritHandle,
	  szName2 // __in_opt  LPCTSTR lpName
	);
	printf("Wait for server!\n");
	fflush(stdout);
	WaitForSingleObject(hSem1, INFINITE);
	printf("Signalling server!\n");
	fflush(stdout);
	ReleaseSemaphore(
		hSem2,	//  __in       HANDLE hSemaphore,
		1, //  __in       LONG lReleaseCount,
		NULL // __out_opt  LPLONG lpPreviousCount
	);
}
