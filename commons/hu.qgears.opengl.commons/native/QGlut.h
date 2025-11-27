#ifndef QGLUT_H
#define QGLUT_H

#include <jni.h>
#include <unistd.h>
#include <GL/freeglut.h>
#include <GL/gl.h>

#define STACK_BUF_SIZE (255u)

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

/**
 * Glew is necessary for freeglut 2.x, but latest freeglut on MAc works without glew (eventually chrashed with glew!)
 * This method is introduced to allow win and linux ports to use glew, and macos to do nothing.
 * 
 * Note that beside the issue with GLEW there are other mandatory initialization steps in freeglut 3.x. On macos port these steps 
 * should go here as well.
 * 
 * Impl must return true if init is successful, false if something fails.
 */
extern bool initializeGlew(void);

extern void mlogImpl(const char* str);

#ifdef QGLUT_LOG
#define mlog(x) mlogImpl(x)
#else
#define mlog(x) 
#endif

#endif /*QGLUT_H*/
