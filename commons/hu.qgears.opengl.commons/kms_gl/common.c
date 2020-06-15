/*
 * Copyright (c) 2017 Rob Clark <rclark@redhat.com>
 * Copyright © 2013 Intel Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sub license,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the
 * next paragraph) shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

#include <errno.h>
#include <fcntl.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"

static struct gbm gbm;

WEAK struct gbm_surface *
gbm_surface_create_with_modifiers(struct gbm_device *gbm,
                                  uint32_t width, uint32_t height,
                                  uint32_t format,
                                  const uint64_t *modifiers,
                                  const unsigned int count);

const struct gbm * init_gbm(int drm_fd, int w, int h, uint64_t modifier)
{
	gbm.dev = gbm_create_device(drm_fd);
	gbm.format = GBM_FORMAT_XRGB8888;
	gbm.surface = NULL;

	if (gbm_surface_create_with_modifiers) {
		gbm.surface = gbm_surface_create_with_modifiers(gbm.dev, w, h,
								gbm.format,
								&modifier, 1);

	}

	if (!gbm.surface) {
		if (modifier != DRM_FORMAT_MOD_LINEAR) {
			fprintf(stderr, "Modifiers requested but support isn't available\n");
			return NULL;
		}
		gbm.surface = gbm_surface_create(gbm.dev, w, h,
						gbm.format,
						GBM_BO_USE_SCANOUT | GBM_BO_USE_RENDERING);

	}

	if (!gbm.surface) {
		printf("failed to create gbm surface\n");
		return NULL;
	}
	uint32_t stride;
//	void * map_data;
//	struct gbm_bo * bo = gbm_surface_lock_front_buffer(gbm.surface);
//	void * mappedBuffer=gbm_bo_map(bo, 0,0,w,h, 0, &stride, &map_data);
//	printf("MApped buffer pointer: %ld\n", (int64_t) mappedBuffer );
//	gbm_surface_release_buffer(gbm.surface, bo);
	gbm.width = w;
	gbm.height = h;

	return &gbm;
}

static bool has_ext(const char *extension_list, const char *ext)
{
	const char *ptr = extension_list;
	int len = strlen(ext);

	if (ptr == NULL || *ptr == '\0')
		return false;

	while (true) {
		ptr = strstr(ptr, ext);
		if (!ptr)
			return false;

		if (ptr[len] == ' ' || ptr[len] == '\0')
			return true;

		ptr += len;
	}
}

static void printFourcc(EGLint value)
{
    union {
      EGLint v;
      char asstr[5];
    } a;
    a.v=value;
    a.asstr[4]=0;
    printf("%s", a.asstr);
}
/// FourCC codes of the GL context and the framebuffer match with ignoring the alpha channel
/// On some HW alpha XRGB is not supported but only ARGB is supported. Which is basically the same thing so we allow that to match.
static int matchFourcc(EGLint a, EGLint b)
{
  return (a&0xFFFFFF00) == (b&0xFFFFFF00);
}

static int
match_config_to_visual(EGLDisplay egl_display,
		       EGLint visual_id,
		       EGLConfig *configs,
		       int count)
{
	int i;

	for (i = 0; i < count; ++i) {
		EGLint id;

		if (!eglGetConfigAttrib(egl_display,
				configs[i], EGL_NATIVE_VISUAL_ID,
				&id))
		{
		  printf("Native visual ID query failed\n");
			continue;
		}
 		  printf("Native visual ID query returns: ");
		  printFourcc(id);
 		  printf(" req: ");
		  printFourcc(visual_id);
 		  printf("\n");
		if (matchFourcc(id, visual_id))
		{
 		  printf("Match OK!\n");
			return i;
		}
	}
	  printf("Could not match visual_id to ELG_NATIVE_VISUAL_ID\n");

	return -1;
}

static bool
egl_choose_config(EGLDisplay egl_display, const EGLint *attribs,
                  EGLint visual_id, EGLConfig *config_out)
{
	EGLint count = 0;
	EGLint matched = 0;
	EGLConfig *configs;
	int config_index = -1;

	if (!eglGetConfigs(egl_display, NULL, 0, &count) || count < 1) {
		printf("No EGL configs to choose from.\n");
		return false;
	}
	configs = (EGLConfig *) malloc(count * sizeof *configs);
	if (!configs)
	{
	    printf("OOM!\n");
		return false;
	}
		
	if (!eglChooseConfig(egl_display, attribs, configs,
			      count, &matched) || !matched) {
		printf("No EGL configs with appropriate attributes.\n");
		goto out;
	}

	printf("There are configs available\n");
	if (!visual_id)
	{
	  printf("Visual ID is zero\n");
		config_index = 0;
	}else
	{
	  printf("Visual ID is non-zero: %d\n", (int)visual_id);
	}

	if (config_index == -1)
	{
		printf("Match config to visual: %d\n", (int)visual_id);
		config_index = match_config_to_visual(egl_display,
						      visual_id,
						      configs,
						      matched);
	}

	if (config_index != -1)
		*config_out = configs[config_index];

out:
	free(configs);
	if (config_index == -1)
		return false;

	return true;
}

int init_egl(struct egl *egl, const struct gbm *gbm, int samples)
{
	UNUSED(samples);
	EGLint major, minor;

	static const EGLint context_attribs[] = {
		EGL_CONTEXT_CLIENT_VERSION, 2,
		EGL_NONE
	};

	const EGLint config_attribs[] = {
//	EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
	EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
          EGL_BLUE_SIZE, 8,
          EGL_GREEN_SIZE, 8,
          EGL_RED_SIZE, 8,
          EGL_DEPTH_SIZE, 8,
          EGL_RENDERABLE_TYPE, EGL_OPENGL_BIT,
/*
		EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
		EGL_RED_SIZE, 1,
		EGL_GREEN_SIZE, 1,
		EGL_BLUE_SIZE, 1,
		EGL_ALPHA_SIZE, 0,
		EGL_RENDERABLE_TYPE, EGL_OPENGL_BIT, // EGL_OPENGL_ES2_BIT,
		EGL_SAMPLES, samples,
*/
		EGL_NONE
	};
	const char *egl_exts_client, *egl_exts_dpy, *gl_exts;

#define get_proc_client(ext, name, type) do { \
		if (has_ext(egl_exts_client, #ext)) \
			egl->name = (type)eglGetProcAddress(#name); \
	} while (0)
#define get_proc_dpy(ext, name, type) do { \
		if (has_ext(egl_exts_dpy, #ext)) \
			egl->name = (type)eglGetProcAddress(#name); \
	} while (0)

#define get_proc_gl(ext, name, type) do { \
		if (has_ext(gl_exts, #ext)) \
			egl->name = (type)eglGetProcAddress(#name); \
	} while (0)

	egl_exts_client = eglQueryString(EGL_NO_DISPLAY, EGL_EXTENSIONS);
	get_proc_client(EGL_EXT_platform_base, eglGetPlatformDisplayEXT, PFNEGLGETPLATFORMDISPLAYEXTPROC);

	if (egl->eglGetPlatformDisplayEXT) {
		egl->display = egl->eglGetPlatformDisplayEXT(EGL_PLATFORM_GBM_KHR,
				gbm->dev, NULL);
	} else {
		egl->display = eglGetDisplay((EGLNativeDisplayType)gbm->dev);
	}

	if (!eglInitialize(egl->display, &major, &minor)) {
		printf("failed to initialize\n");
		return -1;
	}

	egl_exts_dpy = eglQueryString(egl->display, EGL_EXTENSIONS);
	get_proc_dpy(EGL_KHR_image_base, eglCreateImageKHR, PFNEGLCREATEIMAGEKHRPROC);
	get_proc_dpy(EGL_KHR_image_base, eglDestroyImageKHR, PFNEGLDESTROYIMAGEKHRPROC);
	get_proc_dpy(EGL_KHR_fence_sync, eglCreateSyncKHR, PFNEGLCREATESYNCKHRPROC);
	get_proc_dpy(EGL_KHR_fence_sync, eglDestroySyncKHR, PFNEGLDESTROYSYNCKHRPROC);
	get_proc_dpy(EGL_KHR_fence_sync, eglWaitSyncKHR, PFNEGLWAITSYNCKHRPROC);
	get_proc_dpy(EGL_KHR_fence_sync, eglClientWaitSyncKHR, PFNEGLCLIENTWAITSYNCKHRPROC);
	get_proc_dpy(EGL_ANDROID_native_fence_sync, eglDupNativeFenceFDANDROID, PFNEGLDUPNATIVEFENCEFDANDROIDPROC);

	egl->modifiers_supported = has_ext(egl_exts_dpy,
					   "EGL_EXT_image_dma_buf_import_modifiers");

	printf("Using display %p with EGL version %d.%d\n",
			egl->display, major, minor);

	printf("===================================\n");
	printf("EGL information:\n");
	printf("  version: \"%s\"\n", eglQueryString(egl->display, EGL_VERSION));
	printf("  vendor: \"%s\"\n", eglQueryString(egl->display, EGL_VENDOR));
	printf("  client extensions: \"%s\"\n", egl_exts_client);
	printf("  display extensions: \"%s\"\n", egl_exts_dpy);
	printf("===================================\n");

	if (!eglBindAPI( EGL_OPENGL_API )) { // EGL_OPENGL_ES_API)) {
		printf("failed to bind api EGL_OPENGL_ES_API\n");
		return -1;
	}
	if (!egl_choose_config(egl->display, config_attribs, gbm->format,
                               &egl->config)) {
		printf("failed to choose config\n");
		return -1;
	}

	egl->context = eglCreateContext(egl->display, egl->config,
			EGL_NO_CONTEXT, context_attribs);
	if (egl->context == NULL) {
		printf("failed to create context\n");
		return -1;
	}

	egl->surface = eglCreateWindowSurface(egl->display, egl->config,
			(EGLNativeWindowType)gbm->surface, NULL);
	if (egl->surface == EGL_NO_SURFACE) {
		printf("failed to create egl surface\n");
		return -1;
	}

	/* connect the context to the surface */
	eglMakeCurrent(egl->display, egl->surface, egl->surface, egl->context);

	gl_exts = (char *) glGetString(GL_EXTENSIONS);
	printf("OpenGL information:\n");
	printf("  version: \"%s\"\n", glGetString(GL_VERSION));
	printf("  shading language version: \"%s\"\n", glGetString(GL_SHADING_LANGUAGE_VERSION));
	printf("  vendor: \"%s\"\n", glGetString(GL_VENDOR));
	printf("  renderer: \"%s\"\n", glGetString(GL_RENDERER));
	printf("  extensions: \"%s\"\n", gl_exts);
	printf("===================================\n");

	get_proc_gl(GL_OES_EGL_image, glEGLImageTargetTexture2DOES, PFNGLEGLIMAGETARGETTEXTURE2DOESPROC);

	return 0;
}
