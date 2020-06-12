#include <modeset_vsync_api.h>
/*
 * Modeset with vsync - simple API to (Java) application:
 * see modeset-vsync-api.h for documentation.

 * modeset - DRM Double-Buffered VSync'ed Modesetting Example
 *
 * Written 2012 by David Herrmann <dh.herrmann@googlemail.com>
 * Dedicated to the Public Domain.
 */

/*
 * DRM Double-Buffered VSync'ed Modesetting Howto
 * This example extends modeset-double-buffered.c and introduces page-flips
 * synced with vertical-blanks (vsync'ed). A vertical-blank is the time-period
 * when a display-controller pauses from scanning out the framebuffer. After the
 * vertical-blank is over, the framebuffer is again scanned out line by line and
 * followed again by a vertical-blank.
 *
 * Vertical-blanks are important when changing a framebuffer. We already
 * introduced double-buffering, so this example shows how we can flip the
 * buffers during a vertical blank and _not_ during the scanout period.
 *
 * This example assumes that you are familiar with modeset-double-buffered. Only
 * the differences between both files are highlighted here.
 */

#include <errno.h>
#include <fcntl.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>

static int modeset_find_crtc(int fd, drmModeRes *res, drmModeConnector *conn,
			     struct modeset_dev *dev);
static int modeset_create_fb(int fd, struct modeset_buf *buf);
static void modeset_destroy_fb(int fd, struct modeset_buf *buf);
static int modeset_setup_dev(modeset_t * ms, drmModeRes *res, drmModeConnector *conn,
			     struct modeset_dev *dev);
static int modeset_open(int *out, const char *node);
static int modeset_prepare(modeset_t * ms);
static void modeset_cleanup(int fd);
static void modeset_page_flip_event(int fd, unsigned int frame,
				    unsigned int sec, unsigned int usec,
				    void *data);
/*
 * modeset_open() stays the same.
 */

static int modeset_open(int *out, const char *node)
{
	int fd, ret;
	uint64_t has_dumb;

	fd = open(node, O_RDWR | O_CLOEXEC);
	if (fd < 0) {
		ret = -errno;
		fprintf(stderr, "cannot open '%s': %m\n", node);
		return ret;
	}

	if (drmGetCap(fd, DRM_CAP_DUMB_BUFFER, &has_dumb) < 0 ||
	    !has_dumb) {
		fprintf(stderr, "drm device '%s' does not support dumb buffers\n",
			node);
		close(fd);
		return -EOPNOTSUPP;
	}

	*out = fd;
	return 0;
}

/*
 * modeset_buf and modeset_dev stay mostly the same. But 6 new fields are added
 * to modeset_dev: r, g, b, r_up, g_up, b_up. They are used to compute the
 * current color that is drawn on this output device. You can ignore them as
 * they aren't important for this example.
 * The modeset-double-buffered.c example used exactly the same fields but as
 * local variables in modeset_draw().
 *
 * The \pflip_pending variable is true when a page-flip is currently pending,
 * that is, the kernel will flip buffers on the next vertical blank. The
 * \cleanup variable is true if the device is currently cleaned up and no more
 * pageflips should be scheduled. They are used to synchronize the cleanup
 * routines.
 */

struct modeset_dev {
	modeset_t * ms;
	struct modeset_dev *next;

	uint8_t front_buf;
	struct modeset_buf bufs[2];

	drmModeModeInfo mode;
	uint32_t conn;
	uint32_t crtc;
	drmModeCrtc *saved_crtc;

	bool pflip_pending;
	bool cleanup;

//	uint8_t r, g, b;
//	bool r_up, g_up, b_up;
};

static struct modeset_dev *modeset_list = NULL;


struct modeset_buf * modeset_get_current_backbuffer(struct modeset_dev * dev)
{
	return &(dev->bufs[dev->front_buf^1]);
}
struct modeset_buf * modeset_get_current_frontbuffer(struct modeset_dev * dev)
{
	return &(dev->bufs[dev->front_buf]);
}

/*
 * modeset_prepare() stays the same.
 */

static int modeset_prepare(modeset_t * ms)
{
	drmModeRes *res;
	drmModeConnector *conn;
	unsigned int i;
	struct modeset_dev *dev;
	int ret;

	/* retrieve resources */
	res = drmModeGetResources(ms->fd);
	if (!res) {
		fprintf(stderr, "cannot retrieve DRM resources (%d): %m\n",
			errno);
		return -errno;
	}

	/* iterate all connectors */
	for (i = 0; i < res->count_connectors; ++i) {
		/* get information for each connector */
		conn = drmModeGetConnector(ms->fd, res->connectors[i]);
		if (!conn) {
			fprintf(stderr, "cannot retrieve DRM connector %u:%u (%d): %m\n",
				i, res->connectors[i], errno);
			continue;
		}

		/* create a device structure */
		dev = (struct modeset_dev *)malloc(sizeof(*dev));
		memset(dev, 0, sizeof(*dev));
		dev->ms=ms;
		dev->conn = conn->connector_id;

		/* call helper function to prepare this connector */
		ret = modeset_setup_dev(ms, res, conn, dev);
		if (ret) {
			if (ret != -ENOENT) {
				errno = -ret;
				fprintf(stderr, "cannot setup device for connector %u:%u (%d): %m\n",
					i, res->connectors[i], errno);
			}
			printf("Freeing resources for connector\n");
			fflush(stdout);
			free(dev);
			drmModeFreeConnector(conn);
			printf("Freeing resources for connectorDONE\n");
			fflush(stdout);
			continue;
		}

		/* free connector data and link device into global list */
		drmModeFreeConnector(conn);
		dev->next = modeset_list;
		modeset_list = dev;
	}

	/* free resources again */
	drmModeFreeResources(res);
			printf("Modeset_prepare returns\n");
			fflush(stdout);
	return 0;
}

/*
 * modeset_setup_dev() stays the same.
 */

static int modeset_setup_dev(modeset_t * ms, drmModeRes *res, drmModeConnector *conn,
			     struct modeset_dev *dev)
{
	int ret;

	/* check if a monitor is connected */
	if (conn->connection != DRM_MODE_CONNECTED) {
		fprintf(stderr, "ignoring unused connector %u\n",
			conn->connector_id);
		return -ENOENT;
	}

	/* check if there is at least one valid mode */
	if (conn->count_modes == 0) {
		fprintf(stderr, "no valid mode for connector %u\n",
			conn->connector_id);
		return -EFAULT;
	}

	/* copy the mode information into our device structure and into both
	 * buffers */
	memcpy(&dev->mode, &conn->modes[0], sizeof(dev->mode));
	dev->bufs[0].width = conn->modes[0].hdisplay;
	dev->bufs[0].height = conn->modes[0].vdisplay;
	dev->bufs[1].width = conn->modes[0].hdisplay;
	dev->bufs[1].height = conn->modes[0].vdisplay;
	fprintf(stderr, "mode for connector %u is %ux%u\n",
		conn->connector_id, dev->bufs[0].width, dev->bufs[0].height);

	/* find a crtc for this connector */
	ret = modeset_find_crtc(ms->fd, res, conn, dev);
	if (ret) {
		fprintf(stderr, "no valid crtc for connector %u\n",
			conn->connector_id);
		return ret;
	}

	/* create framebuffer #1 for this CRTC */
	ret = modeset_create_fb(ms->fd, &dev->bufs[0]);
	if (ret) {
		fprintf(stderr, "cannot create framebuffer for connector %u\n",
			conn->connector_id);
		return ret;
	}

	/* create framebuffer #2 for this CRTC */
	ret = modeset_create_fb(ms->fd, &dev->bufs[1]);
	if (ret) {
		fprintf(stderr, "cannot create framebuffer for connector %u\n",
			conn->connector_id);
		modeset_destroy_fb(ms->fd, &dev->bufs[0]);
		return ret;
	}

	return 0;
}

/*
 * modeset_find_crtc() stays the same.
 */

static int modeset_find_crtc(int fd, drmModeRes *res, drmModeConnector *conn,
			     struct modeset_dev *dev)
{
	drmModeEncoder *enc;
	unsigned int i, j;
	int32_t crtc;
	struct modeset_dev *iter;

	/* first try the currently conected encoder+crtc */
	if (conn->encoder_id)
		enc = drmModeGetEncoder(fd, conn->encoder_id);
	else
		enc = NULL;

	if (enc) {
		if (enc->crtc_id) {
			crtc = enc->crtc_id;
			for (iter = modeset_list; iter; iter = iter->next) {
				if (iter->crtc == crtc) {
					crtc = -1;
					break;
				}
			}

			if (crtc >= 0) {
				drmModeFreeEncoder(enc);
				dev->crtc = crtc;
				return 0;
			}
		}

		drmModeFreeEncoder(enc);
	}

	/* If the connector is not currently bound to an encoder or if the
	 * encoder+crtc is already used by another connector (actually unlikely
	 * but lets be safe), iterate all other available encoders to find a
	 * matching CRTC. */
	for (i = 0; i < conn->count_encoders; ++i) {
		enc = drmModeGetEncoder(fd, conn->encoders[i]);
		if (!enc) {
			fprintf(stderr, "cannot retrieve encoder %u:%u (%d): %m\n",
				i, conn->encoders[i], errno);
			continue;
		}

		/* iterate all global CRTCs */
		for (j = 0; j < res->count_crtcs; ++j) {
			/* check whether this CRTC works with the encoder */
			if (!(enc->possible_crtcs & (1 << j)))
				continue;

			/* check that no other device already uses this CRTC */
			crtc = res->crtcs[j];
			for (iter = modeset_list; iter; iter = iter->next) {
				if (iter->crtc == crtc) {
					crtc = -1;
					break;
				}
			}

			/* we have found a CRTC, so save it and return */
			if (crtc >= 0) {
				drmModeFreeEncoder(enc);
				dev->crtc = crtc;
				return 0;
			}
		}

		drmModeFreeEncoder(enc);
	}

	fprintf(stderr, "cannot find suitable CRTC for connector %u\n",
		conn->connector_id);
	return -ENOENT;
}

/*
 * modeset_create_fb() stays the same.
 */

static int modeset_create_fb(int fd, struct modeset_buf *buf)
{
	struct drm_mode_create_dumb creq;
	struct drm_mode_destroy_dumb dreq;
	struct drm_mode_map_dumb mreq;
	int ret;

	/* create dumb buffer */
	memset(&creq, 0, sizeof(creq));
	creq.width = buf->width;
	creq.height = buf->height;
	creq.bpp = 32;
	ret = drmIoctl(fd, DRM_IOCTL_MODE_CREATE_DUMB, &creq);
	if (ret < 0) {
		fprintf(stderr, "cannot create dumb buffer (%d): %m\n",
			errno);
		return -errno;
	}
	buf->stride = creq.pitch;
	buf->size = creq.size;
	buf->handle = creq.handle;

	/* create framebuffer object for the dumb-buffer */
	ret = drmModeAddFB(fd, buf->width, buf->height, 24, 32, buf->stride,
			   buf->handle, &buf->fb);
	if (ret) {
		fprintf(stderr, "cannot create framebuffer (%d): %m\n",
			errno);
		ret = -errno;
		goto err_destroy;
	}

	/* prepare buffer for memory mapping */
	memset(&mreq, 0, sizeof(mreq));
	mreq.handle = buf->handle;
	ret = drmIoctl(fd, DRM_IOCTL_MODE_MAP_DUMB, &mreq);
	if (ret) {
		fprintf(stderr, "cannot map dumb buffer (%d): %m\n",
			errno);
		ret = -errno;
		goto err_fb;
	}

	/* perform actual memory mapping */
	buf->map = (uint8_t *)mmap(0, buf->size, PROT_READ | PROT_WRITE, MAP_SHARED,
		        fd, mreq.offset);
	if (buf->map == MAP_FAILED) {
		fprintf(stderr, "cannot mmap dumb buffer (%d): %m\n",
			errno);
		ret = -errno;
		goto err_fb;
	}

	/* clear the framebuffer to 0 */
	memset(buf->map, 0, buf->size);

	return 0;

err_fb:
	drmModeRmFB(fd, buf->fb);
err_destroy:
	memset(&dreq, 0, sizeof(dreq));
	dreq.handle = buf->handle;
	drmIoctl(fd, DRM_IOCTL_MODE_DESTROY_DUMB, &dreq);
	return ret;
}

/*
 * modeset_destroy_fb() stays the same.
 */

static void modeset_destroy_fb(int fd, struct modeset_buf *buf)
{
	struct drm_mode_destroy_dumb dreq;

	/* unmap buffer */
	munmap(buf->map, buf->size);

	/* delete framebuffer */
	drmModeRmFB(fd, buf->fb);

	/* delete dumb buffer */
	memset(&dreq, 0, sizeof(dreq));
	dreq.handle = buf->handle;
	drmIoctl(fd, DRM_IOCTL_MODE_DESTROY_DUMB, &dreq);
}

/*
 * Entry point for modeset initialization.
 */
int modeset_init(modeset_t * ms, const char * card)
{
	memset(ms, 0, sizeof(modeset_t));
	int ret;
	struct modeset_dev *iter;
	struct modeset_buf *buf;

	/* ev is zeroed within the ms structure.
	 * Set this to only the latest version you support. Version 2
	 * introduced the page_flip_handler, so we use that. */
	ms->ev.version = 2;
	ms->ev.page_flip_handler = modeset_page_flip_event;


	ms->epfd=epoll_create(1);
	if(ms->epfd == -1)
	{
		perror("Creating epoll object\n");
		return 1;
	}



	fprintf(stderr, "using card '%s'\n", card);

	/* open the DRM device */
	ret = modeset_open(&(ms->fd), card);
	if (ret)
		goto out_return;
	{
		struct epoll_event ev;

		memset(&ev, 0, sizeof(ev));

		ev.events = EPOLLIN;

		if(epoll_ctl(ms->epfd, EPOLL_CTL_ADD, ms->fd, &ev))
		{
			goto out_close;
		}
	}

	/* prepare all connectors and CRTCs */
	ret = modeset_prepare(ms);
	if (ret)
		goto out_close;

	/* perform actual modesetting on each found connector+CRTC */
	for (iter = modeset_list; iter; iter = iter->next) {
		iter->saved_crtc = drmModeGetCrtc(ms->fd, iter->crtc);
		buf = &iter->bufs[iter->front_buf];
		ret = drmModeSetCrtc(ms->fd, iter->crtc, buf->fb, 0, 0,
				     &iter->conn, 1, &iter->mode);
		if (ret)
			fprintf(stderr, "cannot set CRTC for connector %u (%d): %m\n",
				iter->conn, errno);
	}
	printf("Modeset done\n");
	fflush(stdout);
	return 0;
out_close:
	close(ms->fd);
out_return:
	close(ms->epfd);
	if (ret) {
		errno = -ret;
		fprintf(stderr, "modeset failed with error %d: %m\n", errno);
	} else {
		fprintf(stderr, "exiting\n");
	}
	return ret;
}

/* cleanup everything */
int modeset_dispose(modeset_t * ms)
{
	modeset_cleanup(ms->fd);
	close(ms->epfd);
	return 0;
}

/*
 * modeset_page_flip_event() callback sets the page flipping bit to false on the device structure.
 */
static void modeset_page_flip_event(int fd, unsigned int frame,
				    unsigned int sec, unsigned int usec,
				    void *data)
{
	struct modeset_dev *dev = (struct modeset_dev *)data;

	dev->pflip_pending = false;
}

struct modeset_dev * get_modeset_dev(modeset_t * ms, int index)
{
	struct modeset_dev *iter;
	int i=0;
	for (iter = modeset_list; iter; iter = iter->next) {
		if(i==index)
		{
			return iter;
		}
		i++;
	}
	return NULL;
}

extern int modeset_swapbuffers(struct modeset_dev * dev)
{
	modeset_t * ms=dev->ms;
	struct modeset_buf * buf=modeset_get_current_backbuffer(dev);
	int ret = drmModePageFlip(ms->fd, dev->crtc, buf->fb,
			      DRM_MODE_PAGE_FLIP_EVENT, dev);
	if (ret) {
		fprintf(stderr, "cannot flip CRTC for connector %u (%d): %m\n",
			dev->conn, errno);
		return ret;
	} else {
		dev->front_buf ^= 1;
		dev->pflip_pending = true;
	}
	while(dev->pflip_pending)
	{
		struct epoll_event ev;
		memset(&ev, 0, sizeof(ev));
		epoll_wait(ms->epfd, &ev, 1, -1);
		ret=drmHandleEvent(ms->fd, &(ms->ev));
		if(ret)
		{
			perror("drmHandleEvent error.");
			return 1;
		}
	}
	return 0;
}
static void modeset_cleanup(int fd)
{
	struct modeset_dev *iter;
	drmEventContext ev;
	int ret;

	/* init variables */
	memset(&ev, 0, sizeof(ev));
	ev.version = DRM_EVENT_CONTEXT_VERSION;
	ev.page_flip_handler = modeset_page_flip_event;

	while (modeset_list) {
		/* remove from global list */
		iter = modeset_list;
		modeset_list = iter->next;

		/* if a pageflip is pending, wait for it to complete */
		iter->cleanup = true;
		fprintf(stderr, "wait for pending page-flip to complete...\n");
		while (iter->pflip_pending) {
			ret = drmHandleEvent(fd, &ev);
			if (ret)
				break;
		}

		/* restore saved CRTC configuration */
		if (!iter->pflip_pending)
			drmModeSetCrtc(fd,
				       iter->saved_crtc->crtc_id,
				       iter->saved_crtc->buffer_id,
				       iter->saved_crtc->x,
				       iter->saved_crtc->y,
				       &iter->conn,
				       1,
				       &iter->saved_crtc->mode);
		drmModeFreeCrtc(iter->saved_crtc);

		/* destroy framebuffers */
		modeset_destroy_fb(fd, &iter->bufs[1]);
		modeset_destroy_fb(fd, &iter->bufs[0]);

		/* free allocated memory */
		free(iter);
	}
}
