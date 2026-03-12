/*
MacOS implementation of platform dependent functions in QGlut.h
*/
#include "QGlut.h"

void transcodeeventCharacter(userEvent * ev){
	//TODO check whether this transformation is valid...
	ev->charCode=ev->keyCode;
}

void setupVSync(int swap){
	//nothing to do on windows
	//TODO check this!
}

void render(void){
	//dummy function for satisfying GLUT
}

bool initializeGlew(void){
	glutDisplayFunc(render);
	//GLEW is not required any more for up-to-date freeglut
	return true;
}