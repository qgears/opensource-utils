/*
Linux implementation of platform dependent functions in QGlut.h
*/
#include <GL/glew.h>
#include "QGlut.h"
#include <GL/glxew.h>
#include <GL/glx.h>
#include "x11_keysym2unicode.cpp"
/**
 * Convert character code to raw lower case character code
 * based on modifiers active
 */
void transcodeeventCharacter(userEvent * ev)
{
	Display* fgDisplay = glXGetCurrentDisplay();
	if (fgDisplay != NULL){
		XEvent event;
		if(XPending( fgDisplay ))
		{
			XPeekEvent(fgDisplay, &event );
			KeySym keysym;
			// Get X11 keysym
			XLookupString((XKeyEvent*) &event, NULL, 0, &keysym, NULL );
			int uk = (int)_glfwKeySym2Unicode (keysym);
			ev->charCode = uk;
		}else
		{
			ev->charCode=-1;
		}
	}
}
// As read in forum: http://www.opengl.org/discussion_boards/ubbthreads.php?ubb=showflat&Number=288161
void setupVSync(int swap)
{
	mlog("getcurrentdisplay");
	Display *dpy = glXGetCurrentDisplay();
	if(dpy!=NULL)
	{
		mlog("getcurrentdisplay no null!");
	}
	mlog("getcurrentdrawable");
	GLXDrawable drawable = glXGetCurrentDrawable();
	if(drawable!=None)
	{
		mlog("getcurrentdrawable no null!");
	}
	
	unsigned maxSwap;

	if (drawable) {
		mlog("get GLX_SWAP_INTERVAL_EXT");
//		glXQueryDrawable(dpy, drawable, GLX_SWAP_INTERVAL_EXT, &swap);
		mlog("get GLX_MAX_SWAP_INTERVAL_EXT");
//		glXQueryDrawable(dpy, drawable, GLX_MAX_SWAP_INTERVAL_EXT, &maxSwap);
//		printf("The swap interval is %u and the max swap interval is %u\n", swap, maxSwap);
		mlog("set swap interval");
//		swap=1;
		if(glXSwapIntervalEXT)
		{
			glXSwapIntervalEXT(dpy, drawable, swap);
		}
	}
	
}


bool initializeGlew(void) {
	mlog("glewInit");
	GLenum err = glewInit();
	if (GLEW_OK != err)
	{
		/* Problem: glewInit failed, something is seriously wrong. */
		char msg[STACK_BUF_SIZE];
		snprintf(msg, STACK_BUF_SIZE, "GLEW initialization error: %s\n", glewGetErrorString(err));
		fprintf(stderr, "%s", msg);
		fflush(stderr);
		glutDestroyWindow(glutGetWindow());
		return false;
	}
	return true;
}