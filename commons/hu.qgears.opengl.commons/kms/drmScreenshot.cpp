// drmScreenshot - create screenshot using the DRM API by accessing the framebuffer directly
// root access is required
// Checked on these combinations:
//  * Intel driver laptop
//  ** X - full screen is properly captured into png
//  ** kmscube egl+GLES surface - works but there is visible artifact due to half baked rendering. Hopefully it is only due to unsynchronized access to the memory area

// TODO Synchronization with page flip of the application is necessary
// Saving to image is implemented using Cairo - libpng could be enough dependency or we can also use a simple libqtiff image format

// See also: https://github.com/tiagovignatti/intel-gpu-tools/blob/master/tools/intel_framebuffer_dump.c - this example uses a little bit different Intel specific API - maybe that is from the times when this generic API was not usable yet?

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <xf86drm.h>
#include <xf86drmMode.h>
#include <sys/epoll.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/mman.h>

#include <cairo.h>

static int modeset_open(int *out, const char *node)
{
	int fd, ret;
	uint64_t has_dumb;

	fd = open(node, O_RDWR | O_CLOEXEC);
	if (fd < 0) {
		ret = -1;
		fprintf(stderr, "cannot open '%s': %m\n", node);
		return ret;
	}

	if (drmGetCap(fd, DRM_CAP_DUMB_BUFFER, &has_dumb) < 0 ||
	    !has_dumb) {
		fprintf(stderr, "drm device '%s' does not support dumb buffers\n",
			node);
		close(fd);
		return -1;
	}

	*out = fd;
	return 0;
}

void saveImage(void * ptr, drmModeFBPtr fb)
{
	cairo_surface_t *surface;
	cairo_format_t format;
	char name[80];

				snprintf(name, sizeof(name), "/tmp/fb.png");

				switch (fb->depth) {
				case 16: format = CAIRO_FORMAT_RGB16_565; break;
				case 24: format = CAIRO_FORMAT_RGB24; break;
				case 30: format = CAIRO_FORMAT_RGB30; break;
				case 32: format = CAIRO_FORMAT_ARGB32; break;
				default: format = CAIRO_FORMAT_INVALID; break;
				}

				surface = cairo_image_surface_create_for_data((unsigned char *)ptr, format,
									      fb->width, fb->height, fb->pitch);
				cairo_surface_write_to_png(surface, name);
				cairo_surface_destroy(surface);
}

int main (int argc, char ** argv)
{
	char card[]="/dev/dri/card0";
	int fd, pitch, bo_handle, fb_id, second_fb_id;
	drmModeRes *resources;
	drmModeConnector *connector, *first_good_connector = NULL;
	drmModeEncoder *encoder;
	drmModeModeInfo mode;
	drmModeCrtcPtr orig_crtc;
	struct kms_driver *kms_driver;
	struct kms_bo *kms_bo, *second_kms_bo;
	void *map_buf;
	int ret, i;
	drmModeFBPtr fb;
	int size;
	void *map;
	
	struct drm_gem_flink flink;
	struct drm_gem_open open_arg;
	
	ret=modeset_open(&fd, card);

	resources = drmModeGetResources(fd);
	if(resources == NULL){
		fprintf(stderr, "drmModeGetResources failed: %s\n", strerror(errno));
		goto close_fd;
	}

	/* find the first available connector with modes */
	for(i=0; i < resources->count_connectors; ++i) {
		connector = drmModeGetConnector(fd, resources->connectors[i]);
		if(connector != NULL){
			fprintf(stderr, "connector %d found; mode count: %d; state: ", 
			    connector->connector_id, connector->count_modes);
			    
			if(connector->connection == DRM_MODE_CONNECTED) {
				fprintf(stderr, "connected\n");
				
				if (first_good_connector == NULL) {
				    first_good_connector = connector;
				}
			} else {
			    fprintf(stderr, "disconnected\n");
			}
//				break;
//			drmModeFreeConnector(connector);
		} else {
			fprintf(stderr, "get a null connector pointer\n");
		}
	}
	
	connector = first_good_connector;
	
	if (first_good_connector == NULL) {
		fprintf(stderr, "No active connector found.\n");
		goto free_drm_res;
	}

	mode = connector->modes[0];
	fprintf(stderr, "connector mode 0: %dx%d\n", mode.hdisplay, mode.vdisplay);

	/* find the encoder matching the first available connector */
	for (i = 0; i < resources->count_encoders; ++i) {
		encoder = drmModeGetEncoder(fd, resources->encoders[i]);
		if (encoder == NULL){
            fprintf(stderr, "get a null encoder pointer\n");
            continue;
        } else {
			/* Processing encoder found */
            if (encoder->encoder_id == connector->encoder_id) {
                /* Found a matching encoder */
                break;
            }
			drmModeFreeEncoder(encoder);
		}
	}
	
	if(i == resources->count_encoders){
		fprintf(stderr, "No matching encoder with connector, shouldn't happen\n");
		goto free_drm_res;
	}

	orig_crtc = drmModeGetCrtc(fd, encoder->crtc_id);
	if (orig_crtc == NULL)
	{
	  perror("Get CRTC current state");
	  goto free_first_bo;
	}
	
	printf("CRTC current state get success\n");
	fb = drmModeGetFB(fd, orig_crtc->buffer_id);
	if(fb==NULL)
	{
		perror("FB PTR QUERY");
		exit(1);
	}
	
	printf("FB get success\n");
	/*
	flink.handle = fb->handle;
	if (drmIoctl(fd, DRM_IOCTL_GEM_FLINK, &flink)) {
		perror("DRM_IOCTL_GEM_FLINK");
		drmModeFreeFB(fb);
		exit(1);
	}
	printf("Name: %d\n",flink.name);
	open_arg.name=flink.name;
	open_arg.name = flink.name;
	if (drmIoctl(fd, DRM_IOCTL_GEM_OPEN, &open_arg) != 0) {
		perror("DRM_IOCTL_GEM_OPEN");
		exit(-1);
	}
	*/
	struct drm_mode_map_dumb mreq;
	memset(&mreq, 0, sizeof(mreq));
	mreq.handle = fb->handle;

	ret = drmIoctl(fd, DRM_IOCTL_MODE_MAP_DUMB, &mreq);
	if(ret)
	{
		perror("Map dumb buffer");
		exit(-1);
	}
	printf("Map dumb success!\n");
	size=fb->pitch*fb->height;
	map = (uint8_t *)mmap(0, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, mreq.offset);
	if (map == MAP_FAILED) {
		fprintf(stderr, "cannot mmap dumb buffer (%d): %m\n", errno);
		exit(-1);
	}
	saveImage(map, fb);
	munmap(map, size);

	/* clear the framebuffer to 0 */
	// memset(map, 0, size); --- WORKS!!!
	// Get image, compress and save it!
	drmModeFreeFB(fb);
/*
	// kernel mode setting, wow!
	ret = drmModeSetCrtc(
				fd, encoder->crtc_id, fb_id, 
				0, 0, 	// x, y
				&connector->connector_id, 
				1, 		/ element count of the connectors array above
				&mode);
	if(ret){
		fprintf(stderr, "drmModeSetCrtc failed: %s\n", strerror(errno));
		goto free_first_fb;
	}

	create_bo(kms_driver, mode.hdisplay, mode.vdisplay, 
		&pitch, &second_kms_bo, &bo_handle, draw_buffer_with_cairo);

	// add another FB which is associated with bo
	ret = drmModeAddFB(fd, mode.hdisplay, mode.vdisplay, 24, 32, pitch, bo_handle, &second_fb_id);
	if(ret){
		fprintf(stderr, "drmModeAddFB failed (%ux%u): %s\n",
			mode.hdisplay, mode.vdisplay, strerror(errno));
		goto free_second_bo;
	}
	
	struct flip_context flip_context;
	memset(&flip_context, 0, sizeof flip_context);

	ret = drmModePageFlip(
		fd, encoder->crtc_id, second_fb_id,
		DRM_MODE_PAGE_FLIP_EVENT, &flip_context);
	if (ret) {
		fprintf(stderr, "failed to page flip: %s\n", strerror(errno));
		goto free_second_fb;
	}

	flip_context.fb_id[0] = fb_id;
	flip_context.fb_id[1] = second_fb_id;
	flip_context.current_fb_id = second_fb_id;
	flip_context.crtc_id = encoder->crtc_id;
	flip_context.swap_count = 0;
	gettimeofday(&flip_context.start, NULL);

	// disable stdin buffered i/o and local echo
	struct termios old_tio, new_tio;
	tcgetattr(STDIN_FILENO,&old_tio);
	new_tio = old_tio;
	new_tio.c_lflag &= (~ICANON & ~ECHO);
	tcsetattr(STDIN_FILENO, TCSANOW, &new_tio);

	drmEventContext evctx;
	memset(&evctx, 0, sizeof evctx);
	evctx.version = DRM_EVENT_CONTEXT_VERSION;
	evctx.vblank_handler = NULL;
	evctx.page_flip_handler = page_flip_handler;

	while(1){
		struct timeval timeout = { 
			.tv_sec = 3, 
			.tv_usec = 0 
		};
		fd_set fds;

		FD_ZERO(&fds);
		FD_SET(STDIN_FILENO, &fds);
		FD_SET(fd, &fds);
		ret = select(max(STDIN_FILENO, fd) + 1, &fds, NULL, NULL, &timeout);

		if (ret <= 0) {
			continue;
		} else if (FD_ISSET(STDIN_FILENO, &fds)) {
			char c = getchar();
			if(c == 'q' || c == 27)
				break;
		} else {
			// drm device fd data ready
			ret = drmHandleEvent(fd, &evctx);
			if (ret != 0) {
				fprintf(stderr, "drmHandleEvent failed: %s\n", strerror(errno));
				break;
			}
		}
	}

	ret = drmModeSetCrtc(fd, orig_crtc->crtc_id, orig_crtc->buffer_id,
					orig_crtc->x, orig_crtc->y,
					&connector->connector_id, 1, &orig_crtc->mode);
	if (ret) {
		fprintf(stderr, "drmModeSetCrtc() restore original crtc failed: %m\n");
	}

	// restore the old terminal settings
	tcsetattr(STDIN_FILENO,TCSANOW,&old_tio);


free_second_fb:
	drmModeRmFB(fd, second_fb_id);
	
free_second_bo:
	kms_bo_destroy(&second_kms_bo);
	
free_first_fb:
	drmModeRmFB(fd, fb_id);
	
free_first_bo:
	kms_bo_destroy(&kms_bo);

free_kms_driver:
	kms_destroy(&kms_driver);
*/	
free_first_bo:
	// Nothing allocated
free_drm_res:
	drmModeFreeResources(resources);
close_fd:
	drmClose(fd);
	
out:
	exit(0);
//	return EXIT_SUCCESS;


	ret = modeset_open(&fd, card);
	if (ret)
	{
		perror("Open card");
		exit(1);
	}
	drmModeRes *res=drmModeGetResources(fd);
	for(int i=0; i < res->count_encoders; ++i){ 
		drmModeEncoder * encoder = drmModeGetEncoder(fd, res->encoders[i]);
		if(encoder != NULL){
			fprintf(stderr, "encoder %d found\n", encoder->encoder_id);
//			if(encoder->encoder_id == connector->encoder_id);
//			{
//				break;
//			}
			drmModeFreeEncoder(encoder);
		}
		else
		{
			fprintf(stderr, "get a null encoder pointer\n");
		}
	}
	for (int i = 0; i < res->count_connectors; ++i) {
		/* get information for each connector */
		drmModeConnector *conn = drmModeGetConnector(fd, res->connectors[i]);
		if (!conn) {
			fprintf(stderr, "cannot retrieve DRM connector %u:%u (%d): %m\n",
				i, res->connectors[i], errno);
			continue;
		}
		printf("Connector found!\n");
// 		drmModeGetCrtc(fd, conn.crtc);
	}
	return 0;
}

