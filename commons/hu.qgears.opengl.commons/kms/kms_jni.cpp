#include<stdio.h>
#include<stdlib.h>
#include <stdint.h>

#include "generated/hu_qgears_opengl_kms_KMSNative.h"
#include "jniutil.h"
#define CLASS Java_hu_qgears_opengl_kms_KMSNative_

#include <modeset_vsync_api.h>

static modeset_t ms;

METHODPREFIX(CLASS, jint, init)(ST_ARGS, jstring card)
{
	const char* str = env->GetStringUTFChars(card,0);
	int ret=modeset_init(&ms, str);
	env->ReleaseStringUTFChars(card,str);
	if(ret)
	{
		return 1;
	}
	return 0;
}

METHODPREFIX(CLASS, jint, swapBuffers)(ST_ARGS, jint index)
{
	struct modeset_dev * dev=get_modeset_dev(&ms, index);
	modeset_swapbuffers(dev);
	return 0;
}

METHODPREFIX(CLASS, jobject, getBufferPtr)(ST_ARGS, jint devIndex, jint bufferIndex)
{
	struct modeset_dev * dev=get_modeset_dev(&ms, devIndex);
	struct modeset_buf * buf=modeset_getBufferByIndex(dev, bufferIndex);
	jobject bb = env->NewDirectByteBuffer((void*) buf->map, buf->stride*buf->height);
	return bb;
}

METHODPREFIX(CLASS, jint, getCurrentFrontBufferIndex)(ST_ARGS, jint devIndex)
{
	struct modeset_dev * dev=get_modeset_dev(&ms, devIndex);
	int ret=modeset_get_current_frontbuffer_index(dev);
	return ret;
}

METHODPREFIX(CLASS, void, dispose)(ST_ARGS)
{
  modeset_dispose(&ms);
}

METHODPREFIX(CLASS, jint, getBufferParam)(ST_ARGS, jint devIndex, jint bufferIndex, jint paramIndex)
{
	struct modeset_dev * dev=get_modeset_dev(&ms, devIndex);
	struct modeset_buf * buf=modeset_getBufferByIndex(dev, bufferIndex);
	switch(paramIndex)
	{
		case 0:
			return buf->width;
		case 1:
			return buf->height;
		case 2:
			return buf->stride;
	}
	return -1;	
}

