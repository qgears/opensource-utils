#include "generated/hu_qgears_opengl_x11_X11Gl.h"
#define CLASS Java_hu_qgears_opengl_x11_X11Gl_
#define EXCCLASS "hu/qgears/opengl/x11/X11GlException"
#include "jniutil.h"

#include<stdio.h>
#include<stdlib.h>
#include<X11/X.h>
#include<X11/Xlib.h>
#include <GL/glew.h>
#include <GL/glxew.h>
#include<GL/gl.h>
#include<GL/glx.h>
#include<GL/glu.h>

#include "x11_keysym2unicode.cpp"

// Java callbacks:
jmethodID callbackMouseID;
jmethodID callbackKeyboardID;
jmethodID callbackResizeID;

Display                 *dpy;
Window                  root;
GLint                   att[] = { GLX_RGBA, GLX_ALPHA_SIZE, 8, GLX_DEPTH_SIZE, 0, GLX_DOUBLEBUFFER, None };
XVisualInfo             *vi;
Colormap                cmap;
XSetWindowAttributes    swa;
Window                  win;
GLXContext              glc;
XWindowAttributes       gwa;
XEvent                  xev;

void mlog(const char * str)
{
	printf("%s\n", str);
	fflush(stdout);
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
	dpy = XOpenDisplay(NULL);
	if(dpy == NULL) {
		JNU_ThrowByName(env, EXCCLASS, "cannot connect to X server");
		return;
	}
}
Atom wmDeleteMessage;

METHODPREFIX(CLASS, void, openWindow)(ST_ARGS,
	jboolean initFullscreen, jstring titlej,
		jint x, jint y,
		jint width, jint height)
{
	root = DefaultRootWindow(dpy);

	vi = glXChooseVisual(dpy, 0, att);

	if(vi == NULL) {
		JNU_ThrowByName(env, EXCCLASS, "no appropriate visual found");
		return;
	}
	else {
		printf("\n\tvisual %p selected\n", (void *)vi->visualid); /* %p creates hexadecimal output like in glxinfo */
	}
	cmap = XCreateColormap(dpy, root, vi->visual, AllocNone);

	swa.colormap = cmap;
	swa.event_mask = ExposureMask | KeyPressMask
		| ButtonPressMask | ButtonReleaseMask
//		| ButtonMotionMask
		| PointerMotionMask | StructureNotifyMask;
	// TODO freeglut specifies these masks:
	//        StructureNotifyMask | SubstructureNotifyMask | ExposureMask |
//        ButtonPressMask | ButtonReleaseMask | KeyPressMask | KeyReleaseMask |
 //       VisibilityChangeMask | EnterWindowMask | LeaveWindowMask |
  //      PointerMotionMask | ButtonMotionMask;
	
	
	const char * title = env->GetStringUTFChars(titlej, NULL);

	win = XCreateWindow(dpy, root, x, y, 
		width, height, 0, vi->depth, InputOutput, vi->visual, CWColormap | CWEventMask, &swa);
	// register interest in the delete window message
	wmDeleteMessage = XInternAtom(dpy, "WM_DELETE_WINDOW", False);
	XSetWMProtocols(dpy, win, &wmDeleteMessage, 1);	
	XStoreName(dpy, win, title);
	env->ReleaseStringUTFChars(titlej, title);
	glc = glXCreateContext(dpy, vi, NULL, GL_TRUE);
	glXMakeCurrent(dpy, win, glc);
	
	mlog("glewInit");
	GLenum err = glewInit();
	if (GLEW_OK != err)
	{
		// Problem: glewInit failed, something is seriously wrong.
		char msg[255];
		sprintf(msg, "GLEW initialization error: %s\n", glewGetErrorString(err));
		fprintf(stderr, "%s", msg);
		fflush(stderr);
		JNU_ThrowByName(env, EXCCLASS, "GLEW initialization error");
		return;
	}
}
METHODPREFIX(CLASS, void, showWindow)(ST_ARGS)
{
	XMapWindow(dpy, win);
}
METHODPREFIX(CLASS, void, swapBuffers)(ST_ARGS)
{
	glXSwapBuffers(dpy, win);
}
jboolean exitReq=0;
METHODPREFIX(CLASS, void, mainLoop)(ST_ARGS)
{
	while(!exitReq&&XPending(dpy))
	{
		XNextEvent(dpy, &xev);
		switch(xev.type)
		{
			case Expose:
			{
				XExposeEvent * eev=(XExposeEvent *) &xev;
				env->CallVoidMethod(obj, callbackResizeID, -1, -1);
			}
			case ConfigureNotify:
			{
				XConfigureEvent * eev=(XConfigureEvent *) &xev;
				env->CallVoidMethod(obj, callbackResizeID, eev->width, eev->height);
			}
			break;
			case MotionNotify:
			{
				XMotionEvent * mev=(XMotionEvent *)&xev;
				env->CallVoidMethod(obj, callbackMouseID, 2, mev->x, mev->y, -1);
			}
			break;
			case ButtonRelease:
			{
				XButtonEvent * bev=(XButtonEvent *)&xev;
				env->CallVoidMethod(obj, callbackMouseID, 0, bev->x, bev->y, bev->button);
			}
			break;
			case ButtonPress:
			{
				XButtonEvent * bev=(XButtonEvent *)&xev;
				env->CallVoidMethod(obj, callbackMouseID, 1, bev->x, bev->y, bev->button);
			}	
			break;	
			case KeyPress:
			case KeyRelease:
			{
				XKeyEvent * kev=(XKeyEvent *)&xev;
				KeySym keysym;
				// Get X11 keysym
				XLookupString(kev, NULL, 0, &keysym, NULL );
				// Find unicode character
				int uniCode = (int)_glfwKeySym2Unicode (keysym);
				env->CallVoidMethod(obj, callbackKeyboardID,
					xev.type==KeyPress? 1:0,
					kev->x, kev->y,
					kev->keycode, kev->state, uniCode);
			}
			break;
			case ClientMessage:
				if (xev.xclient.data.l[0] == wmDeleteMessage)
				{
					exitReq = 1;
				}
			break;
			default:
			break;
		}
	}
}
METHODPREFIX(CLASS, jboolean, isCloseRequested)(ST_ARGS)
{
	return exitReq;
}
METHODPREFIX(CLASS, void, dispose)(ST_ARGS)
{
	glXMakeCurrent(dpy, None, NULL);
	glXDestroyContext(dpy, glc);
	XDestroyWindow(dpy, win);
	XCloseDisplay(dpy);
}

