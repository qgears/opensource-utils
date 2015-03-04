#include "generated/hu_qgears_opengl_mirgl_MirGl.h"
#define CLASS Java_hu_qgears_opengl_mirgl_MirGl_
#define EXCCLASS "hu/qgears/opengl/mirgl/MirGlException"
#include "jniutil.h"

#include "mir_toolkit/mir_client_library.h"
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <time.h>
#include <string.h>
#include <EGL/egl.h>

#include<pthread.h>

#include <xkbcommon/xkbcommon-keysyms.h>

#include "x11_keysym2unicode.cpp"

// Java callbacks:
jmethodID callbackMouseID;
jmethodID callbackKeyboardID;
jmethodID callbackResizeID;

static MirConnection *connection;
static MirSurface *surface;
static EGLDisplay egldisplay;
static EGLSurface eglsurface;
static volatile sig_atomic_t running = 0;
static int app_width=0;
static int app_height=0;
#define N_EVENT 4096

pthread_mutex_t events_lock;
static MirEvent events[N_EVENT];
static int evReadPtr = 0;
static int evWritePtr = 0;

void mlog(const char * str)
{
	printf("%s\n", str);
	fflush(stdout);
}
#define CHECK(_cond, _err) \
	if (!(_cond)) \
	{ \
		JNU_ThrowByName(env, EXCCLASS, _err); \
		return; \
	}

	
static void mir_glapp_handle_event(MirSurface* surface, MirEvent const* ev, void* context)
{
	pthread_mutex_lock(&events_lock);
	int ptr=evWritePtr+1;
	ptr%=N_EVENT;
	if(ptr!=evReadPtr)
	{
		memcpy(&(events[evWritePtr]), ev, sizeof(MirEvent));
		events[evWritePtr]=*ev;
		evWritePtr=ptr;
	}
	pthread_mutex_unlock(&events_lock);
}

static const MirDisplayOutput *find_active_output(
	const MirDisplayConfiguration *conf)
{
	const MirDisplayOutput *output = NULL;
	int d;

	for (d = 0; d < (int)conf->num_outputs; d++)
	{
		const MirDisplayOutput *out = conf->outputs + d;

		if (out->used &&
			out->connected &&
			out->num_modes &&
			out->current_mode < out->num_modes)
		{
			output = out;
			break;
		}
	}

	return output;
}

	
METHODPREFIX(CLASS, void, init)(ST_ARGS)
{
	jclass cls = env->GetObjectClass(obj);
	callbackMouseID = env->GetMethodID(cls, "callbackMouse", "(IIII)V");
	if(callbackMouseID==0)
	{
		JNU_ThrowByName(env, EXCCLASS, "callbackMouse method not found");
		return;
	}
	callbackKeyboardID = env->GetMethodID(cls, "callbackKeyboard", "(IIIIII)V");
	if(callbackKeyboardID==0)
	{
		JNU_ThrowByName(env, EXCCLASS, "callbackKeyboard method not found");
		return;
	}
	callbackResizeID = env->GetMethodID(cls, "callbackResize", "(II)V");
	if(callbackResizeID==0)
	{
		JNU_ThrowByName(env, EXCCLASS, "callbackResize method not found");
		return;
	}
	int err=pthread_mutex_init(&events_lock, NULL);
	CHECK(!err, "Create mutex lock");
	mlog("Open display");
	char mir_socket[]="/tmp/mir_socket";
	char appname[]="Mir Demo";
	connection = mir_connect_sync(mir_socket, appname);
	// TODO log mir socket file
	mlog("connect mir");
	CHECK(mir_connection_is_valid(connection), "Cannot connect Mir server.");
	mlog("connected mir");
}

METHODPREFIX(CLASS, void, openWindow)(ST_ARGS,
	jboolean initFullscreen, jstring titlej,
		jint x, jint y,
		jint width, jint height)
{
	EGLint ctxattribs[] =
	{
		EGL_CONTEXT_CLIENT_VERSION, 2,
		EGL_NONE
	};
	MirSurfaceParameters surfaceparm =
	{
		"eglappsurface",
		256, 256,
		mir_pixel_format_xbgr_8888,
		mir_buffer_usage_hardware,
		mir_display_output_id_invalid
	};
	MirEventDelegate delegate = 
	{
		mir_glapp_handle_event,
		NULL
	};
	mlog("mir_connection_create_display_config");
	MirDisplayConfiguration* display_config = mir_connection_create_display_config(connection);
	mlog("find_active_output");
	const MirDisplayOutput *output = find_active_output(display_config);

	CHECK(output, "No active outputs found.");
	const MirDisplayMode *mode = &output->modes[output->current_mode];
	MirPixelFormat format[mir_pixel_formats];
	unsigned int nformats;

	mlog("mir_connection_get_available_surface_formats");
	mir_connection_get_available_surface_formats(connection,
		format, mir_pixel_formats, &nformats);

	surfaceparm.pixel_format = format[0];
	for (unsigned int f = 0; f < nformats; f++)
	{
		float mir_eglapp_background_opacity=1.0f;
		const int opaque = (format[f] == mir_pixel_format_xbgr_8888 ||
							format[f] == mir_pixel_format_xrgb_8888 ||
							format[f] == mir_pixel_format_bgr_888);

		if ((mir_eglapp_background_opacity == 1.0f && opaque) ||
			(mir_eglapp_background_opacity < 1.0f && !opaque))
		{
			surfaceparm.pixel_format = format[f];
			break;
		}
	}
	printf("Current active output is %dx%d %+d%+d\n",
		mode->horizontal_resolution, mode->vertical_resolution,
		output->position_x, output->position_y);

	surfaceparm.width = width > 0 ? width : mode->horizontal_resolution;
	surfaceparm.height = height > 0 ? height : mode->vertical_resolution;

	mir_display_config_destroy(display_config);
	
	printf("Server supports %d of %d surface pixel formats. Using format: %d\n",
		nformats, mir_pixel_formats, surfaceparm.pixel_format);
	unsigned int bpp = 8 * MIR_BYTES_PER_PIXEL(surfaceparm.pixel_format);
	EGLint attribs[] =
	{
		EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
		EGL_RENDERABLE_TYPE, EGL_OPENGL_BIT,
		EGL_COLOR_BUFFER_TYPE, EGL_RGB_BUFFER,
		EGL_BUFFER_SIZE, bpp,
		EGL_NONE
	};

	surface = mir_connection_create_surface_sync(connection, &surfaceparm);
	CHECK(mir_surface_is_valid(surface), "Can't create a surface");
	mlog("Surface created!");
	
	mir_surface_set_event_handler(surface, &delegate);

	egldisplay = eglGetDisplay(
					(EGLNativeDisplayType)mir_connection_get_egl_native_display(connection));
	CHECK(egldisplay != EGL_NO_DISPLAY, "Can't eglGetDisplay");

	EGLBoolean ok = eglInitialize(egldisplay, NULL, NULL);
	CHECK(ok, "Can't eglInitialize");

	ok=eglBindAPI(EGL_OPENGL_API);
	CHECK(ok, "Can't bind OpenGL API");
	mlog("OpenGL API bound");
	EGLint neglconfigs;
	
	EGLConfig eglconfig;
	ok = eglChooseConfig(egldisplay, attribs, &eglconfig, 1, &neglconfigs);
	CHECK(ok, "Could not eglChooseConfig");
	CHECK(neglconfigs > 0, "No EGL config available");
	
	eglsurface = eglCreateWindowSurface(egldisplay, eglconfig,
			(EGLNativeWindowType)mir_surface_get_egl_native_window(surface),
			NULL);
	CHECK(eglsurface != EGL_NO_SURFACE, "eglCreateWindowSurface failed");

	EGLContext eglctx= eglCreateContext(egldisplay, eglconfig, EGL_NO_CONTEXT,
							ctxattribs);
	CHECK(eglctx != EGL_NO_CONTEXT, "eglCreateContext failed");
	ok = eglMakeCurrent(egldisplay, eglsurface, eglsurface, eglctx);
	CHECK(ok, "Can't eglMakeCurrent");
	
	app_width = surfaceparm.width;
	app_height = surfaceparm.height;
	
	EGLint swapinterval = 1;
	eglSwapInterval(egldisplay, swapinterval);

	running = 1;
}
METHODPREFIX(CLASS, void, showWindow)(ST_ARGS)
{
}
METHODPREFIX(CLASS, void, swapBuffers)(ST_ARGS)
{
	static time_t lasttime = 0;
	static int lastcount = 0;
	static int count = 0;
	time_t now = time(NULL);
	time_t dtime;
	int dcount;
	EGLint width, height;

	if (!running)
		return;

	eglSwapBuffers(egldisplay, eglsurface);

	count++;
	dcount = count - lastcount;
	dtime = now - lasttime;
	if (dtime)
	{
		printf("%d FPS\n", dcount);
		lasttime = now;
		lastcount = count;
	}

	/*
	 * Querying the surface (actually the current buffer) dimensions here is
	 * the only truly safe way to be sure that the dimensions we think we
	 * have are those of the buffer being rendered to. But this should be
	 * improved in future; https://bugs.launchpad.net/mir/+bug/1194384
	 */
	if (eglQuerySurface(egldisplay, eglsurface, EGL_WIDTH, &width) &&
		eglQuerySurface(egldisplay, eglsurface, EGL_HEIGHT, &height))
	{
		app_width=width;
		app_height=height;
	}
}
void processEvent(ST_ARGS, const MirEvent *ev)
{
	if (ev->type == mir_event_type_key)
	{
		if(ev->key.action == mir_key_action_up || ev->key.action == mir_key_action_down )
		{
			KeySym keysym=ev->key.key_code;
			// Get X11 keysym
			// SDL: MIR_xkb_keysym_to_utf8
			// Find unicode character
			int uniCode = (int)_glfwKeySym2Unicode (keysym);
			env->CallVoidMethod(obj, callbackKeyboardID,
				ev->key.action==mir_key_action_down? 1:0,
				0,0,
				ev->key.scan_code, ev->key.modifiers, uniCode);
		}
	}else if(ev->type ==  mir_event_type_motion)
	{
		int x=(int)ev->motion.pointer_coordinates[0].x;
		int y=(int)ev->motion.pointer_coordinates[0].y;
		int button=ev->motion.button_state;
		if(ev->motion.action==mir_motion_action_down)
		{
			env->CallVoidMethod(obj, callbackMouseID, 1, x, y, button);
		}
		else if(ev->motion.action==mir_motion_action_up)
		{
			env->CallVoidMethod(obj, callbackMouseID, 0, x, y, button);
		}
		else if(ev->motion.action==mir_motion_action_move)
		{
			env->CallVoidMethod(obj, callbackMouseID, 2, x, y, -1);
		}
	}
	else if (ev->type == mir_event_type_resize)
	{
		printf("Resized to %dx%d\n", ev->resize.width, ev->resize.height);
	}
}
METHODPREFIX(CLASS, void, mainLoop)(ST_ARGS)
{
	MirEvent ev;
	int processed;
	do
	{
		pthread_mutex_lock(&events_lock);
		if(evReadPtr!=evWritePtr)
		{
			processed=1;
			memcpy(&ev, &(events[evReadPtr]), sizeof(MirEvent));
			evReadPtr++;
			evReadPtr%=N_EVENT;
		}else
		{
			processed=0;
		}
		pthread_mutex_unlock(&events_lock);
		if(processed)
		{
			processEvent(env, obj, &ev);
		}
	}while(processed);
}
METHODPREFIX(CLASS, jboolean, isCloseRequested)(ST_ARGS)
{
	return !running;
}
METHODPREFIX(CLASS, void, dispose)(ST_ARGS)
{
}

