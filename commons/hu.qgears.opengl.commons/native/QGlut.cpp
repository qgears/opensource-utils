#include "generated/hu_qgears_opengl_glut_Glut.h"
#define CLASS Java_hu_qgears_opengl_glut_Glut_
#define EXCCLASS "hu/qgears/opengl/glut/GlutException"
#include "jniutil.h"

#include "QGlut.h"

//definition of exit
#include <stdlib.h>

// EVENT_TYPES
// Normal keyboard down event
#define EVENT_KEYBOARD_DOWN 0
// Normal keyboard up event
#define EVENT_KEYBOARD_UP 1
// Special character keyboard down event
#define EVENT_SPECIAL_DOWN 2
// Special character keyboard up event
#define EVENT_SPECIAL_UP 3
// Normal mouse event
#define EVENT_MOUSE 4
// Normal mouse motion event
#define EVENT_MOUSE_MOTION 5


#define MAX_EVENT 2048
userEvent eventRR[MAX_EVENT];

int eventReadPtr=0;
int eventWritePtr=0;

float angle=0;

void initialize () 
{
    glMatrixMode(GL_PROJECTION);												// select projection matrix
    glViewport(0, 0, 1024, 768);									// set the viewport
    glMatrixMode(GL_PROJECTION);												// set matrix mode
    glLoadIdentity();															// reset projection matrix
    GLfloat aspect = (GLfloat) 1024.0f / 768.0f;
	gluPerspective(45, aspect, 1, 500);		// set up a perspective projection matrix
    glMatrixMode(GL_MODELVIEW);													// specify which matrix is the current matrix
    glShadeModel( GL_SMOOTH );
    glClearDepth( 1.0f );														// specify the clear value for the depth buffer
    glEnable( GL_DEPTH_TEST );
    glDepthFunc( GL_LEQUAL );
    glHint( GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST );						// specify implementation-specific hints
	glClearColor(0.0, 0.0, 0.0, 1.0);											// specify clear values for the color buffers								
}

int disp()		// Here's Where We Do All The Drawing
{
//	mlog("haromszog");
	initialize();
    // Clear Screen And Depth Buffer
    glClear(GL_COLOR_BUFFER_BIT);
    // Reset The Current Modelview Matrix
    glMatrixMode(GL_MODELVIEW);
    glTranslatef(0.0f,0.0f,-3.0f);
    glLoadIdentity();
    glColor3f(1,0,0);

//NEW//////////////////NEW//////////////////NEW//////////////////NEW/////////////

    glTranslatef(-1.5f,0.0f,-6.0f);	// Move 1.5 Left And 6.0 Into The Screen.
    glRotatef(angle*180.0f/3.141592654f,0,0,1);
    glBegin(GL_TRIANGLES);		// Drawing Using Triangles
	glVertex3f( 0.0f, 1.0f, 0.0f);		// Top
	glVertex3f(-1.0f,-1.0f, 0.0f);		// Bottom Left
	glVertex3f( 1.0f,-1.0f, 0.0f);		// Bottom Right
    glEnd();					// Finished Drawing

    glTranslatef(3.0f,0.0f,0.0f);			// Move Right
    glBegin(GL_QUADS);				// Draw A Quad
	glVertex3f(-1.0f, 1.0f, 0.0f);		// Top Left
	glVertex3f( 1.0f, 1.0f, 0.0f);		// Top Right
	glVertex3f( 1.0f,-1.0f, 0.0f);		// Bottom Right
	glVertex3f(-1.0f,-1.0f, 0.0f);		// Bottom Left
    glEnd();
    
	angle+=3.141592645*2/60/10;
	if(angle>3.141592654)
	{
		angle-=3.141592654;
		angle-=3.141592654;
	}


    return 1;			// Keep Going
}

void disp0(void){

  // do  a clearscreen
  glClear(GL_COLOR_BUFFER_BIT);
  
  // draw something
	glMatrixMode(GL_MODELVIEW);
	
	glPushMatrix();
  glRotatef(angle*180/3.141592654, 0, 0, 1);

  glutWireTeapot(0.5);
	glPopMatrix();
// One turn is 10 seconds in case we do exactly 60 FPS
	angle+=3.141592645*2/60/10;
	if(angle>3.141592654)
	{
		angle-=3.141592654;
		angle-=3.141592654;
	}
//	glutPostRedisplay();
//	ctr++;
//	if(!(ctr%60))
//	{
//		cout<<ctr<<endl;
//	}
}


//#define mlog(a)

METHODPREFIX(CLASS, void, nativeTest)(ST_ARGS)
{
	int margc=0;
	mlog("glutInit");
	glutInit(&margc, NULL);
  // specify the display mode to be RGB and single buffering 
  // we use single buffering since this will be non animated
  	mlog("glutInitDisplayMode");
	glutInitDisplayMode(GLUT_RGBA | GLUT_DOUBLE);

	mlog("glutGameModeString");

	glutGameModeString( "1024x768" ); //the settings for fullscreen mode
	mlog("glutEnterGameMode");
	
	glutEnterGameMode(); //set glut to fullscreen using the settings in the line above

	if (initializeGlew() == false ){
		JNU_ThrowByName(env, EXCCLASS, "Glew init failed. see stderr.");
	}
	mlog("setupVSync");
	setupVSync(1);
	mlog("Do loop");
	int i=0;
	while(i<60*10)
	{
		mlog("Event");
//		glutMainLoopEvent();
		mlog("Display");
		disp();
		mlog("swap");
//		usleep(1000*14);
		glutSwapBuffers();
		i++;
	}
}

METHODPREFIX(CLASS, void, nativeInit0)(ST_ARGS)
{
	mlog("Hello");
	int margc=0;
	mlog("glutInit");
	glutInit(&margc, NULL);
	// specify the display mode to be RGB and single buffering 
	// we use single buffering since this will be non animated
  	mlog("glutInitDisplayMode");
	glutInitDisplayMode(GLUT_RGBA | GLUT_DOUBLE);

	mlog("glutGameModeString");

	glutGameModeString( "1024x768" ); //the settings for fullscreen mode
	mlog("glutEnterGameMode");

	glutEnterGameMode(); //set glut to fullscreen using the settings in the line above

	if (initializeGlew() == false ){
		JNU_ThrowByName(env, EXCCLASS, "Glew init failed. see stderr.");
	}

	mlog("setupVSync");
	setupVSync(1);
}

METHODPREFIX(CLASS, void, init)(ST_ARGS)
{
	mlog("Hello");
	int margc=0;
	mlog("glutInit");
	glutInit(&margc, NULL);
}

userEvent * writeNextEvent()
{
	if((eventWritePtr+1)%MAX_EVENT!=eventReadPtr)
	{
		userEvent * ret=&(eventRR[eventWritePtr]);
		eventWritePtr++;
		eventWritePtr%=MAX_EVENT;
		return ret;
	}else
	{
		return NULL;
	}
}

void specialFunc(int key, int x, int y)
{
	userEvent * ev=writeNextEvent();
	if(ev!=NULL)
	{
		ev->type=EVENT_SPECIAL_DOWN;
		ev->keyCode=key;
		ev->x=x;
		ev->y=y;
		ev->charCode = -1;//by definition the special key does not have a corresponding charcode
	}
}
void specialUpFunc(int key, int x, int y)
{
	userEvent * ev=writeNextEvent();
	if(ev!=NULL)
	{
		ev->type=EVENT_SPECIAL_UP;
		ev->keyCode=key;
		ev->x=x;
		ev->y=y;
		ev->charCode = -1;//by definition the special key does not have a corresponding charcode
	}
}


void keyboardFunc(unsigned char key, int x, int y)
{
	userEvent * ev=writeNextEvent();
	if(ev!=NULL)
	{
		ev->type=EVENT_KEYBOARD_DOWN;
		ev->keyCode=key;
		ev->x=x;
		ev->y=y;
		ev->state=glutGetModifiers();
		transcodeeventCharacter(ev);
/*		printf("%d\n", unicodeKey);
		printf("%c\n", unicodeKey);
		fflush(stdout);*/
	}
}
void keyboardUpFunc(unsigned char key, int x, int y)
{
	userEvent * ev=writeNextEvent();
	if(ev!=NULL)
	{
		ev->type=EVENT_KEYBOARD_UP;
		ev->keyCode=key;
		ev->x=x;
		ev->y=y;
		ev->state=glutGetModifiers();
		transcodeeventCharacter(ev);
	}
}


void motionFunc(int x, int y)
{
	userEvent * ev=writeNextEvent();
	if(ev!=NULL)
	{
		ev->type=EVENT_MOUSE_MOTION;
		ev->x=x;
		ev->y=y;
	}
}
void mouseFunc(int button, int state, int x, int y)
{
	userEvent * ev=writeNextEvent();
	if(ev!=NULL)
	{
		ev->type=EVENT_MOUSE;
		ev->x=x;
		ev->y=y;
		ev->button=button;
		ev->state=state;
	}
//	printf("Mouse: %d %d\n", x, y);
//	fflush(stdout);
}

METHODPREFIX(CLASS, void, setWindowTitle)(ST_ARGS, jstring titlej)
{
	const char * title = env->GetStringUTFChars(titlej, NULL);
	glutSetWindowTitle(title);
	env->ReleaseStringUTFChars(titlej, title);
	
}
METHODPREFIX(CLASS, void, setupVSync2)(ST_ARGS, jint n)
{
	setupVSync(n);
}
METHODPREFIX(CLASS, void, nativeInit)(ST_ARGS, jboolean fullscreen, jint width, jint height)
{
	// specify the display mode to be RGB and single buffering 
	// we use single buffering since this will be non animated

	mlog("glutInitDisplayMode");
	glutInitDisplayMode(GLUT_RGBA | GLUT_ALPHA | GLUT_DOUBLE);

	// this is needed to let the application start in VirtualBox, as described:
	// https://groups.google.com/forum/?fromgroups=#!topic/comp.graphics.api.opengl/Oecgo2Fc9Zc
	if (!glutGet(GLUT_DISPLAY_MODE_POSSIBLE))
		exit(1);

    if(fullscreen)
	{
		mlog("glutGameModeString");
		char modestring[STACK_BUF_SIZE];
		snprintf(modestring, STACK_BUF_SIZE, "%dx%d", width, height);
		glutGameModeString( modestring ); //the settings for fullscreen mode
		mlog("glutEnterGameMode");
	
		glutEnterGameMode(); //set glut to fullscreen using the settings in the line above
	}else
	{
		glutInitWindowSize (width, height);
//		glutInitDisplayMode ( GLUT_RGB | GLUT_DOUBLE | GLUT_DEPTH);
		glutCreateWindow ("s");
	}
	
	/// Listeners must be registered _after_ window is created!
	glutSetKeyRepeat(GLUT_KEY_REPEAT_OFF);
	glutKeyboardFunc(keyboardFunc);
	glutKeyboardUpFunc(keyboardUpFunc);
	glutSpecialFunc(specialFunc);
	glutSpecialUpFunc(specialUpFunc);
	glutMotionFunc(motionFunc);
	glutPassiveMotionFunc(motionFunc);
	glutMouseFunc(mouseFunc);
	
	
	if (initializeGlew() == false ){
		JNU_ThrowByName(env, EXCCLASS, "Glew init failed. see stderr.");
	}
//	mlog("setupVSync");
//	setupVSync();
}

METHODPREFIX(CLASS, void, testDrawBasicScene)(ST_ARGS)
{
	disp();
}
METHODPREFIX(CLASS, void, swapBuffers)(ST_ARGS)
{
	glutSwapBuffers();
}

METHODPREFIX(CLASS, void, mainLoopEvent)(ST_ARGS)
{
	glutPostRedisplay();
	glutMainLoopEvent();
}

METHODPREFIX(CLASS, jint, getScreenHeight)(ST_ARGS)
{
	return glutGet(GLUT_SCREEN_HEIGHT);
}
METHODPREFIX(CLASS, jint, getScreenWidth)(ST_ARGS)
{
	return glutGet(GLUT_SCREEN_WIDTH);
}
METHODPREFIX(CLASS, jint, getWindowHeight)(ST_ARGS)
{
	return glutGet(GLUT_WINDOW_HEIGHT);
}
METHODPREFIX(CLASS, jint, getWindowWidth)(ST_ARGS)
{
	return glutGet(GLUT_WINDOW_WIDTH);
}
METHODPREFIX(CLASS, void, setFullScreen)(ST_ARGS, jboolean fullscreen, jint width, jint height)
{
	if(fullscreen)
	{
		glutFullScreen();
	}else
	{
		glutReshapeWindow(width, height);
	}
}

METHODPREFIX(CLASS, jobject, getMessagesBuffer)(ST_ARGS)
{
    return env->NewDirectByteBuffer(&eventRR, sizeof(eventRR));
}
METHODPREFIX(CLASS, jint, getAndResetMessagesReadIndex)(ST_ARGS)
{
	int ret=eventReadPtr;
	eventReadPtr=eventWritePtr;
	return ret;
}
METHODPREFIX(CLASS, jint, getMessagesWriteIndex)(ST_ARGS)
{
	return eventWritePtr;
}
//do not use this method directly. Use mlog
void mlogImpl(const char * str)
{
	printf("%s\n", str);
	fflush(stdout);
}


METHODPREFIX(CLASS,jlong,getFunctionAddressNative)(ST_ARGS,jobject functionName) {
	void * functionNameBytes=env->GetDirectBufferAddress(functionName);
	return (jlong)glutGetProcAddress((char*)functionNameBytes);
}

