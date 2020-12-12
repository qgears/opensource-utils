/*
 * Copyright (c) 2017 Rob Clark <rclark@redhat.com>
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
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/syscall.h>

#include <gbm.h>
#include <sys/stat.h>

#include "common.h"
#include "drm-common.h"

static void page_flip_handler(int fd, unsigned int frame,
		  unsigned int sec, unsigned int usec, void *data);

static struct drm drm;

static drmEventContext evctx = {
			.version = 2,
			.vblank_handler = NULL,
			.page_flip_handler = page_flip_handler,
	};
static struct gbm_bo *bo;
static struct drm_fb *fb;
	

struct gbm_device {
   /* Hack to make a gbm_device detectable by its first element. */
   struct gbm_device *(*dummy)(int);

   int fd;
   const char *name;
   unsigned int refcount;
   struct stat stat;

   void (*destroy)(struct gbm_device *gbm);
   int (*is_format_supported)(struct gbm_device *gbm,
                              uint32_t format,
                              uint32_t usage);

   struct gbm_bo *(*bo_create)(struct gbm_device *gbm,
                               uint32_t width, uint32_t height,
                               uint32_t format,
                               uint32_t usage);
   struct gbm_bo *(*bo_import)(struct gbm_device *gbm, uint32_t type,
                               void *buffer, uint32_t usage);
   int (*bo_write)(struct gbm_bo *bo, const void *buf, size_t data);
   void (*bo_destroy)(struct gbm_bo *bo);

   struct gbm_surface *(*surface_create)(struct gbm_device *gbm,
                                         uint32_t width, uint32_t height,
                                         uint32_t format, uint32_t flags);
   struct gbm_bo *(*surface_lock_front_buffer)(struct gbm_surface *surface);
   void (*surface_release_buffer)(struct gbm_surface *surface,
                                  struct gbm_bo *bo);
   int (*surface_has_free_buffers)(struct gbm_surface *surface);
   void (*surface_destroy)(struct gbm_surface *surface);
};

struct gbm_surface {
   struct gbm_device *gbm;
   uint32_t width;
   uint32_t height;
   uint32_t format;
   uint32_t flags;
};

struct gbm_kms_surface {
    struct gbm_surface base;
    struct gbm_kms_bo *bo[2];
    int front;
    int (*set_bo)(struct gbm_kms_surface *, int, void *, int, uint32_t);
};

static void page_flip_handler(int fd, unsigned int frame,
		  unsigned int sec, unsigned int usec, void *data)
{
	/* suppress 'unused parameter' warnings */
	(void)fd, (void)frame, (void)sec, (void)usec;

	int *waiting_for_flip = (int *)data;
	*waiting_for_flip = 0;
}
void legacy_away()
{
		drmModeSetCrtc(drm.fd,
				       drm.saved_crtc->crtc_id,
				       drm.saved_crtc->buffer_id,
				       drm.saved_crtc->x,
				       drm.saved_crtc->y,
				       &drm.connector_id,
				       1,
				       &(drm.saved_crtc->mode));
}
void legacy_back()
{
	int ret = drmModeSetCrtc(drm.fd, drm.crtc_id, fb->fb_id, 0, 0,
			&drm.connector_id, 1, drm.mode);
	if (ret) {
		printf("failed to set mode: %s\n", strerror(errno));
		return;
	}
			
}
void legacy_dispose()
{
		drmModeSetCrtc(drm.fd,
				       drm.saved_crtc->crtc_id,
				       drm.saved_crtc->buffer_id,
				       drm.saved_crtc->x,
				       drm.saved_crtc->y,
				       &drm.connector_id,
				       1,
				       &(drm.saved_crtc->mode));
		drmModeFreeCrtc(drm.saved_crtc);
}

int legacy_beforefirstframe(const struct gbm *gbm, const struct egl *egl)
{
	eglSwapBuffers(egl->display, egl->surface);
	bo = gbm_surface_lock_front_buffer(gbm->surface);
	
	if (NULL == bo) {
	   fprintf(stderr, "legacy_beforefirstframe: Failed to get a new framebuffer BO\n");
	   print_trace();
	   exit(-1);
	}
	
	fb = drm_fb_get_from_bo(bo);
	if (!fb) {
		fprintf(stderr, "legacy_beforefirstframe: Failed to get a new framebuffer\n");
		exit(-1);
	}
	drm.saved_crtc=drmModeGetCrtc(drm.fd, drm.crtc_id);
	return 0;
}

int legacy_firstframe(const struct gbm *gbm, const struct egl *egl)
{
	/* set mode: */
	int ret = drmModeSetCrtc(drm.fd, drm.crtc_id, fb->fb_id, 0, 0,
			&drm.connector_id, 1, drm.mode);
	if (ret) {
		printf("failed to set mode: %s\n", strerror(errno));
		return ret;
	}
	return 0;
}

int legacy_nextframe(const struct gbm *gbm, const struct egl *egl)
{
    	fd_set fds;

		struct gbm_bo *next_bo;
		int waiting_for_flip = 1;

//		egl->draw(i++);

		eglSwapBuffers(egl->display, egl->surface);
		
		fprintf(stderr, "legacy_nextframe on thread %d\n", syscall(__NR_gettid));
        print_trace();
		
		next_bo = gbm_surface_lock_front_buffer(gbm->surface);
		
        if (!next_bo) {
           fprintf(stderr, "XXX legacy_nextframe: Failed to get next framebuffer BO; thread_id: %d\n", syscall(__NR_gettid));
           exit(-1);
        }
    
		fb = drm_fb_get_from_bo(next_bo);
		if (!fb) {
			fprintf(stderr, "legacy_nextframe: Failed to get a new framebuffer\n");
			exit(-1);
		}

		/*
		 * Here you could also update drm plane layers if you want
		 * hw composition
		 */

		int ret = drmModePageFlip(drm.fd, drm.crtc_id, fb->fb_id,
				DRM_MODE_PAGE_FLIP_EVENT, &waiting_for_flip);
		if (ret) {
			printf("failed to queue page flip: %s\n", strerror(errno));
			return -1;
		}

		while (waiting_for_flip) {
			FD_ZERO(&fds);
			FD_SET(0, &fds);
			FD_SET(drm.fd, &fds);

			ret = select(drm.fd + 1, &fds, NULL, NULL, NULL);
			if (ret < 0) {
				if(errno==EINTR)
				{
					// EINTR - maybe a terminal switch was executed? Retry...
					continue;
				}
				else
				{
					printf("select err: %s\n", strerror(errno));
					return ret;
				}
			} else if (ret == 0) {
				printf("select timeout!\n");
				return -1;
			} else if (FD_ISSET(0, &fds)) {
				fprintf(stderr, "user interrupted!\n");
				return 0;
			}
			drmHandleEvent(drm.fd, &evctx);
		}

		/* release last buffer to render on again: */
		gbm_surface_release_buffer(gbm->surface, bo);
		bo = next_bo;
	return 0;
}


static int legacy_run(const struct gbm *gbm, const struct egl *egl)
{
    printf("legacy_run() with legacy_firstframe() and nextframe endless loop");
	legacy_firstframe(gbm, egl);
	while (1) {
		legacy_nextframe(gbm, egl);
	}

	return 0;
}

const struct drm * init_drm_legacy(const char *device)
{
	int ret;

	ret = init_drm(&drm, device);
	if (ret)
		return NULL;

	drm.run = legacy_run;

	return &drm;
}
