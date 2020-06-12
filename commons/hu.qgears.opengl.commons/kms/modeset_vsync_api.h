// #define _GNU_SOURCE

#include <stdint.h>
#include <xf86drm.h>
#include <xf86drmMode.h>
#include <sys/epoll.h>

typedef struct {
	// DRM device descriptor
	int fd;
	// Epoll file descriptor - used to wait for page flip finished event
	int epfd;
	// DRM event processing context
	drmEventContext ev;
} modeset_t;

struct modeset_dev;
/** A single buffer to draw onto */
struct modeset_buf {
	uint32_t width;
	uint32_t height;
	uint32_t stride;
	uint32_t size;
	uint32_t handle;
	uint8_t *map;
	uint32_t fb;
};



/**
 * Initialize modesetting. Find all devices and create a front and backbuffer for them.
 */
extern int modeset_init(modeset_t * ms, const char * card);
/**
 * Close modesetting session before application exits.
 */
extern int modeset_dispose(modeset_t * ms);
/**
 * After modeset_init get the devices indexed from 0.
 * @return NULL if there is no device with that index.
 * The returned struct is valid until dispose.
 */
extern struct modeset_dev * get_modeset_dev(modeset_t * ms, int index);

/**
 * Get the buffer where drawing has to be done onto.
 * After the bufferSwap call this will be the frontbuffer.
 * Valid after modeset_init even before the first modeset_swapbuffers.
 * This way the first visible frame can already be initialized with a valid image.
 * The returned struct is valid until swapbuffers or dispose.
 */
extern struct modeset_buf * modeset_get_current_backbuffer(struct modeset_dev * dev);
extern struct modeset_buf * modeset_getBufferByIndex(struct modeset_dev * dev, uint32_t index);
/**
 * Get the buffer currently being shown to the user.
 * Must be accessed read-only to avoid visible glitches.
 */
extern struct modeset_buf * modeset_get_current_frontbuffer(struct modeset_dev * dev);
extern int modeset_get_current_frontbuffer_index(struct modeset_dev * dev);
/**
 * Swap front and backbuffers.
 * The call is blocking until buffer swap happens on the hardware.
 * @return nonzero means error
 */
extern int modeset_swapbuffers(struct modeset_dev * dev);

