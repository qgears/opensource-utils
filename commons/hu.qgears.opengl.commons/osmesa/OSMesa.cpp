//#include <math.h>
//#include <stdio.h>
//#include <stdlib.h>
//#include <string.h>

#include "generated/hu_qgears_opengl_osmesa_OSMesa.h"
#define CLASS Java_hu_qgears_opengl_osmesa_OSMesa_
#define EXCCLASS "hu/qgears/opengl/osmesa/OSMesaException"
#include "jniutil.h"
//#include "extgl.h"

#include "GL/osmesa.h"

OSMesaContext osMesaCtx;

METHODPREFIX(CLASS, void, createContext)(ST_ARGS)
{
	osMesaCtx = OSMesaCreateContextExt( OSMESA_RGBA, 0, 0, 0, NULL );
}
METHODPREFIX(CLASS, void, makeCurrentPrivate)(ST_ARGS, jobject image, jint width, jint height)
{
//	printf("GL version: %llu \n", glGetString);
    void * buffer=env->GetDirectBufferAddress(image);
	if(!OSMesaMakeCurrent(osMesaCtx, buffer, GL_UNSIGNED_BYTE, width, height))
	{
		JNU_ThrowByName(env, EXCCLASS, "Context initialization error");
	}
//	printf("GL version: %llu %d %s\n", glGetString, GL_VERSION, glGetString(GL_VERSION));
//	printf("extgl returns: %llu\n", (jlong)(intptr_t)extgl_GetProcAddress((char *)"glGetString"));
//	fflush(stdout);
}

METHODPREFIX(CLASS, void, disposeContext)(ST_ARGS)
{
	OSMesaDestroyContext( osMesaCtx );
}
