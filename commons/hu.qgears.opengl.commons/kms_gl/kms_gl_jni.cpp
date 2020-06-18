#include<stdio.h>
#include<stdlib.h>
#include <stdint.h>
#include "common.h"
#include "drm-common.h"
#include "psplash-console.h"

#include "generated/hu_qgears_opengl_kmsgl_KMSGLNative.h"
#include "jniutil.h"
#define CLASS Java_hu_qgears_opengl_kmsgl_KMSGLNative_

static struct egl egl;
static const struct gbm *gbm;
static const struct drm * drm;
static bool firstFrame=true;
static bool dirty=true;

METHODPREFIX(CLASS, jint, init)(ST_ARGS, jstring card)
{
	const char* str = env->GetStringUTFChars(card,0);
	drm = init_drm_legacy(str);
	env->ReleaseStringUTFChars(card,str);
	if(drm==NULL)
	{
		return 1;
	}
	gbm = init_gbm(drm->fd, drm->mode->hdisplay, drm->mode->vdisplay,
			DRM_FORMAT_MOD_LINEAR);
	if (!gbm) {
		printf("failed to initialize GBM\n");
		return -1;
	}
	if(init_egl(&egl, gbm, 0))
	{
		printf("failed to initialize EGL\n");
		return -1;
	}
	if(legacy_beforefirstframe(gbm, &egl))
	{
		return -1;
	}
	
	firstFrame=true;
	return 0;
}

METHODPREFIX(CLASS, jint, swapBuffers)(ST_ARGS, jint index)
{
	if(psplash_is_active())
	{
		if(firstFrame)
		{
			firstFrame=false;
			psplash_console_switch();
			if(legacy_firstframe(gbm, &egl))
			{
				return 1;
			}
		}else
		{
			if(legacy_nextframe(gbm, &egl))
			{
				return 2;
			}
		}
		dirty=false;
	}
	return 0;
}

METHODPREFIX(CLASS, void, dispose)(ST_ARGS)
{
	psplash_console_reset();
	legacy_dispose();
}
METHODPREFIX(CLASS, jint, handleVTSwitch)(ST_ARGS)
{
	if(psplash_is_req_away())
	{
		legacy_away();
		psplash_away_ack();
		dirty=false;
		return 1;
	}
	if(psplash_is_req_back())
	{
		legacy_back();
		psplash_back_ack();
		dirty=true;
		return 2;
	}
	return 0;
}
METHODPREFIX(CLASS, jint, getBufferParam)(ST_ARGS, jint devIndex, jint bufferIndex, jint paramIndex)
{
	switch(paramIndex)
	{
		case 0:
			return gbm->width;
		case 1:
			return gbm->height;
		case 2:
		{
			return psplash_is_active();
		}
		case 3:
		{
			return dirty;
		}
	}
	return -1;	
}

