#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <IL/il.h>

// The exception type that is thrown when something goes wrong on the native side
#define EXCCLASS "hu/qgears/images/devil/DevILException"


typedef struct 
{
	ILuint name;
	int width;
	int height;
	char * data; 
} DevILImage;

