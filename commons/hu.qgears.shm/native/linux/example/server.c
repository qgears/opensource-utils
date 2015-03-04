/*
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <fcntl.h>
#include <sys/ipc.h>
#include <sys/shm.h>
*/
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include<talloc.h>
// gcc -ltalloc -lrt server.c
typedef struct
{
	long long size;
} rglshm;
void syncFlush(void * ptr, long long length)
{
	msync(ptr, length, MS_INVALIDATE);
}
int main(int argc, char **argv)
{
	char shmName[]="/rgl";
	int shmemid=shm_open(shmName, O_RDWR|O_CREAT|
//		O_EXCL|
		O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP);
	if(shmemid<0)
	{
		fprintf(stderr, "Error createing shmem\n");
		return -1;
	}
	// One gig, please!
	long	long length=1024*1024*1024;
	int ret=ftruncate(shmemid, length);
	if(ret<0)
	{
		fprintf(stderr, "Error setting size of shmem\n");
		return -2;
	}
	rglshm * ptr=(rglshm *)mmap(NULL, length, PROT_READ | PROT_WRITE, MAP_SHARED, shmemid, 0);
	if (ptr == NULL)
	{
		perror("In mmap()");
		return -3;
	}
	printf("Shared memory segment allocated correctly (%lld bytes).\n", length);
/*	char * cptr=(char *)ptr;
	for(int i=0;i<length; ++i)
	{
		*cptr=i%256;
		cptr++;
	}
*/
	void * mempool = talloc_pool(ptr, 20000);//length);
	void * t1=talloc_size(mempool, 1024*1024*512);
	void * t2=talloc_size(mempool, 1024);
	void * t3=talloc_size(mempool, 1024);
	talloc_free(t2);
	void * t4=talloc_size(mempool, 1024);
	printf("ptr: %lld pool: %lld t1:%lld t2:%lld t3:%lld t4:%lld\n", (long long)ptr, 
		(long long)mempool, (long long)t1, (long long)t2, 
		(long long)t3, (long long)t4);
	fflush(stdout);
	ret=mlock(ptr, length);
	if(ret!=0)
	{
		perror("In mlock()");
		return -4;
	}
	printf("Shared memory segment locked!\n");
	ret=munlock(ptr, length);
	if(ret!=0)
	{
		perror("In munlock()");
		return -5;
	}
//	printf("Message type is %d, content is: %s\n", shared_msg->type, shared_msg->content);
	munmap(ptr, length);
	close(shmemid);
	return 0;
}


