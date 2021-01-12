#include<stdio.h>
#include<stdlib.h>
#include <stdint.h>

#include <libudev.h>
#include <libinput.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include "generated/hu_qgears_opengl_libinput_LibinputNative.h"
#include "jniutil.h"
#define CLASS Java_hu_qgears_opengl_libinput_LibinputNative_
#define MAX_INPUT_EVENT 1024

typedef struct  __attribute__((packed))
{
	uint32_t type;
	uint32_t a;
	uint32_t b;
	uint32_t c;
	double da;
	double db;
} inputevent;

typedef struct lijni_tag
{
	struct udev * udev;
	struct libinput* li;
	const struct libinput_interface *  	iface;
	inputevent inputEvents[MAX_INPUT_EVENT];
	uint32_t currWriteInputEvent=0;
} lijni_t;

static lijni_t lijni;
static lijni_t * plijni;
static inputevent *allocateEvent()
{
	inputevent * ret=&(plijni->inputEvents[plijni->currWriteInputEvent]);
	plijni->currWriteInputEvent++;
	plijni->currWriteInputEvent%=MAX_INPUT_EVENT;
	// TODO check overflow
	return ret;
}

static int open_restricted(const char *path, int flags, void *user_data)
{
	int fd = open(path, flags);
	if(fd<0)
	{
		fprintf(stderr, "Libinput Open error: %s to %d\n", path, fd);
	}
    return fd < 0 ? -errno : fd;
}
static void close_restricted(int fd, void *user_data)
{
    close(fd);
}
const struct libinput_interface li_iface = {
	open_restricted,
	close_restricted,
};

METHODPREFIX(CLASS, jint, init)(ST_ARGS)
{
  plijni=&lijni;
  plijni->udev = udev_new();
  plijni->iface = &li_iface;
  plijni->li = libinput_udev_create_context(plijni->iface,
		plijni,
		plijni->udev);
  libinput_udev_assign_seat(plijni->li, "seat0");
  return 0;
}
METHODPREFIX(CLASS, jobject, getInputBuffer)(ST_ARGS)
{
	return env->NewDirectByteBuffer((void*) (plijni->inputEvents), sizeof(plijni->inputEvents));
}
METHODPREFIX(CLASS, jint, getInputBufferStrip)(ST_ARGS)
{
	return sizeof(plijni->inputEvents)/MAX_INPUT_EVENT;
}
METHODPREFIX(CLASS, jint, poll)(ST_ARGS)
{
  inputevent * ev;
  libinput_dispatch(plijni->li);
  struct libinput_event *event;
  plijni->currWriteInputEvent=0;
  while ((event = libinput_get_event(plijni->li)) != NULL)
  {
    fprintf(stderr, "libinput event\n");
    switch(libinput_event_get_type(event))
    {
    	case LIBINPUT_EVENT_KEYBOARD_KEY:
    	{
    	   	struct libinput_event_keyboard* keyb = libinput_event_get_keyboard_event(event);
		   	if(keyb!=NULL)
		   	{
		   		ev=allocateEvent();
		   		ev->type = 300;
		   	 	ev->a=libinput_event_keyboard_get_key(keyb);
		   	 	ev->b=libinput_event_keyboard_get_key_state(keyb);
		   	}
    		break;
    	}
    	case LIBINPUT_EVENT_POINTER_MOTION:
    	{
    		struct libinput_event_pointer * ptr = libinput_event_get_pointer_event(event);
    		if(ptr!=NULL)
    		{
		   		ev=allocateEvent();
		   		ev->type = 400;
		   	 	ev->da=libinput_event_pointer_get_dx(ptr);
		   	 	ev->db=libinput_event_pointer_get_dy(ptr);
    		}
    		break;
    	}
    	case LIBINPUT_EVENT_POINTER_MOTION_ABSOLUTE:
    	{
    		struct libinput_event_pointer * ptr = libinput_event_get_pointer_event(event);
    		if(ptr!=NULL)
    		{
		   		ev=allocateEvent();
		   		ev->type = 401;
		   	 	ev->da=libinput_event_pointer_get_absolute_x_transformed(ptr, 1);
		   	 	ev->db=libinput_event_pointer_get_absolute_y_transformed(ptr, 1);
    		}
    		break;
    	}
    	case LIBINPUT_EVENT_POINTER_BUTTON:
    	{
    		struct libinput_event_pointer * ptr = libinput_event_get_pointer_event(event);
    		if(ptr!=NULL)
    		{
		   		ev=allocateEvent();
		   		ev->type=402;
		   		ev->a=libinput_event_pointer_get_button(ptr);
		   		ev->b=libinput_event_pointer_get_button_state(ptr);
    		}
    		break;
    	}
    	default:
            fprintf(stderr, "unhandled libinput event type\n");
    		break;
    		// Do not handle other event types 
    }
    libinput_event_destroy(event);
    libinput_dispatch(plijni->li);
  }
  return plijni->currWriteInputEvent;	
}

METHODPREFIX(CLASS, void, dispose)(ST_ARGS)
{
  udev_unref(plijni->udev);
}
