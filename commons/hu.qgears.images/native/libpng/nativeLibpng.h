#include <stdlib.h>
#include <stdio.h>
#include <math.h>

// The exception type that is thrown when something goes wrong on the native side
#define EXCCLASS "hu/qgears/images/libpng/NativeLibPngException"


typedef struct 
{
	png_structp png_ptr;
	png_infop info_ptr;
	int width;
	int height;
	int nChannel;
	int rowBytes;
	png_bytep* row_pointers;

// Memory reader structure
	int readAt;
	int readSize;
	unsigned char * data;

// Memory writer structure
	char * destination;
	unsigned int writeSize;
	unsigned int writePosition;
	int writeError;
} PngImage;

