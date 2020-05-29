//#include <math.h>
#include <stdio.h>
#include <stdlib.h>
//#include <string.h>
#include <dlfcn.h>

#include <inttypes.h>

#include "generated/hu_qgears_opengl_osmesa_OSMesaNative.h"
#define CLASS Java_hu_qgears_opengl_osmesa_OSMesaNative_
#define EXCCLASS "hu/qgears/opengl/osmesa/OSMesaException"
#include "jniutil.h"
//#include "extgl.h"

#include "GL/osmesa.h"

OSMesaContext osMesaCtx;

METHODPREFIX(CLASS, void, createContext)(ST_ARGS)
{
	osMesaCtx = OSMesaCreateContextExt( OSMESA_ARGB, 0, 0, 0, NULL );
}
static void * lib_gl_handle = NULL;
METHODPREFIX(CLASS, void, execPreload)(ST_ARGS_STATIC)
{
	printf("execPreload\n");
	putenv("LD_LIBRARY_PATH=/home/rizsi/git-qgears/fos/opensource-utils/commons/hu.qgears.opengl.commons/src/hu/qgears/opengl/osmesa/");
	lib_gl_handle = dlopen("libGL.so.1", RTLD_LAZY | RTLD_GLOBAL);
//	if (lib_gl_handle == NULL) {
//		fprintf(stderr, "OSMesa preload - Error loading libGL.so.1: %s\n", dlerror());
//	}
//glXGetProcAddressARB(const GLubyte * procName)
//	/home/rizsi/git-qgears/fos/opensource-utils/commons/hu.qgears.opengl.commons/osmesa/linux/libGL.so.1
}
METHODPREFIX(CLASS, void, makeCurrentPrivate)(ST_ARGS, jobject image, jint width, jint height)
{
    void * buffer=env->GetDirectBufferAddress(image);
	if(!OSMesaMakeCurrent(osMesaCtx, buffer, GL_UNSIGNED_BYTE, width, height))
	{
		JNU_ThrowByName(env, EXCCLASS, "Context initialization error");
	}
	printf("GL version: %s\n", (char *)glGetString(GL_VERSION));
//	printf("extgl returns: %llu\n", (jlong)(intptr_t)extgl_GetProcAddress((char *)"glGetString"));
	fflush(stdout);
}

METHODPREFIX(CLASS, void, disposeContext)(ST_ARGS)
{
	OSMesaDestroyContext( osMesaCtx );
}
