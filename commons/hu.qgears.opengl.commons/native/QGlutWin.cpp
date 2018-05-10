/*
Windows implementation of platform dependent functions in QGlut.h
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