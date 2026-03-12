#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>

#include <inttypes.h>

#include "generated/hu_qgears_opengl_osmesa_OSMesaNative.h"
#define CLASS Java_hu_qgears_opengl_osmesa_OSMesaNative_
#define EXCCLASS "hu/qgears/opengl/osmesa/OSMesaException"
#include "jniutil.h"

#include "GL/osmesa.h"

OSMesaContext osMesaCtx;

METHODPREFIX(CLASS, void, createContext)(ST_ARGS, jint modeIndex)
{
	int mode=OSMESA_ARGB;
	switch(modeIndex)
	{
		case 0:
		 mode=OSMESA_ARGB;
		 break;
		case 1:
		 mode=OSMESA_BGRA;
		 break;
		case 2:
		 mode=OSMESA_RGBA;
		 break;
	}
	osMesaCtx = OSMesaCreateContextExt( mode, 0, 0, 0, NULL );
}
METHODPREFIX(CLASS, void, makeCurrentPrivate)(ST_ARGS, jobject image, jint width, jint height)
{
    void * buffer=env->GetDirectBufferAddress(image);
	if(!OSMesaMakeCurrent(osMesaCtx, buffer, GL_UNSIGNED_BYTE, width, height))
	{
		JNU_ThrowByName(env, EXCCLASS, "Context initialization error");
	}
}
METHODPREFIX(CLASS, jstring, getGlVersion)(ST_ARGS)
{
	return env->NewStringUTF((char *)glGetString(GL_VERSION));
}

METHODPREFIX(CLASS, void, disposeContext)(ST_ARGS)
{
	OSMesaDestroyContext( osMesaCtx );
}


#define OSMESALIB_OLD "libOSMesa.so" ## <-- TODO problematic on macos
#define OSMESALIB "libOSMesa.dylib"

METHODPREFIX(CLASS, void, checkOsMesaLoadable)(ST_ARGS)
{
	/*
	void* mesa_handle=dlopen(OSMESALIB, RTLD_LAZY | RTLD_GLOBAL);
	if(mesa_handle==NULL)
	{
		char logBuf[100] = {0};
		snprintf(logBuf,sizeof(logBuf),"dlopen cant load " OSMESALIB ": %s",dlerror());
		logBuf[99] = '\0';
		JNU_ThrowByName(env, EXCCLASS, logBuf);
	} else {
		dlclose(mesa_handle);
	}
	*/
}
