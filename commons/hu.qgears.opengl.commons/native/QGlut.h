#ifndef QGLUT_H
#define QGLUT_H

#include <jni.h>
#include <unistd.h>
#include <GL/glew.h>
#include <GL/freeglut.h>
#include <GL/gl.h>

typedef struct
{
	// When this struct is changed the Glut.java / messageSize must also be changed.
	jint type;
	// Mouse coordinate when the event is received.
	jint x;
	// Mouse coordinate when the event is received.
	jint y;
	union
	{
		// The mouse button index.
		jint button;
		// The code of the key or special key code that is received from Glut
		jint keyCode;
	};
	// mouse state (mouse button event) or glutGetModifiers(keyboard event)
	jint state;
	jint charCode;
} __attribute__((packed)) userEvent;

/**
 * Convert character code to raw lower case character code
 * based on modifiers active
 */
extern void transcodeeventCharacter(userEvent * ev);

extern void setupVSync(int swap);

extern void mlogImpl(const char* str);

#ifdef QGLUT_LOG
#define mlog(x) mlogImpl(x)
#else
#define mlog(x) 
#endif

#endif /*QGLUT_H*/