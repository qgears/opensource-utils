// Tricky libGL.so.1 implementation that
// connects libosmesa to lwjgl
// Compile:
// gcc -fPIC -shared osmesapreload.c -o libGL.so.1
// Use:
// export LD_LIBRARY_PATH='pathtoosmesapreloadfolder'/linGL.so.1
// run the lwjgl application that uses osmesa

#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include "GL/osmesa.h"

typedef unsigned char GLubyte;
typedef int GLint;

// Handle to the loaded osmesa library
static void * lib_gl_handle = NULL;

// This is the only method that lwjgl calls directly.
// lwjgl gets the address of all methods dynamically using this method.
// By overriding libGL.so.1 with LD_LIBRARY_PATH this library is loaded
// instead of the nvidia/mesa supproted libGL.so.1
// This method then loads libOSMesa32.so to find the openGL
// methods software implementation there and connects them to lwjgl
void * glXGetProcAddressARB(const GLubyte * procName)
{
	if(lib_gl_handle==NULL)
	{
		lib_gl_handle=dlopen("libOSMesa.so", RTLD_LAZY | RTLD_GLOBAL);
		if(lib_gl_handle==NULL)
		{
			printf("Cant load libosmesa\n");
			fflush(stdout);
			exit(-1);
		}
	}
// TODO bug for some reason it is not defined though man dlsym says it should be defined in dlfcn.h
#define RTLD_DEFAULT 0
	void * ret=dlsym(RTLD_DEFAULT,procName);
	if(ret==NULL)
	{
//		printf("Method missing: %s\n", procName);
//		fflush(stdout);
	}
	return ret;
}
