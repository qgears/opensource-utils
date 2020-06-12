#include<stdio.h>
#include<stdlib.h>
#include <stdint.h>

#include "generated/hu_qgears_opengl_kms_KMSNative.h"
#include "jniutil.h"
#define CLASS Java_hu_qgears_opengl_kms_KMSNative_

#include <modeset_vsync_api.h>

static modeset_t ms;

METHODPREFIX(CLASS, jint, init)(ST_ARGS)
{
	/* TODO parameter which DRM device to open and which to draw onto. */
	const char *card="/dev/dri/card0";

	if(modeset_init(&ms, card))
	{
		return 1;
	}
	printf("Modesetinited\n");
	fflush(stdout);
	return 0;
//	struct modeset_dev * dev=get_modeset_dev(&ms, 0);
}

METHODPREFIX(CLASS, jint, swapBuffers)(ST_ARGS, jint index)
{
	struct modeset_dev * dev=get_modeset_dev(&ms, index);
	modeset_swapbuffers(dev);
	return 0;
}

METHODPREFIX(CLASS, jobject, getCurrentBackBufferPtr)(ST_ARGS, jint index)
{
	struct modeset_dev * dev=get_modeset_dev(&ms, index);
	struct modeset_buf * buf=modeset_get_current_backbuffer(dev);
	jobject bb = env->NewDirectByteBuffer((void*) buf->map, buf->stride*buf->height);
	return bb;
}

METHODPREFIX(CLASS, void, dispose)(ST_ARGS)
{
  modeset_dispose(&ms);
}
