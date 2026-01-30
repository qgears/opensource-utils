/*
Windows implementation of platform dependent functions in QGlut.h
*/
#include <GL/glew.h>
#include "QGlut.h"

void transcodeeventCharacter(userEvent * ev){
	//TODO check whether this transformation is valid...
	ev->charCode=ev->keyCode;
}

void setupVSync(int swap){
	//nothing to do on windows
	//TODO check this!
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