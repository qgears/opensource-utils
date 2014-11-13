#include <stdint.h>
/*************************************************
 * IFD directory entry in TIFF image file format *
 *************************************************/
typedef struct {
	int16_t tag;			//the identifier of directory entry
	int16_t type;			//the type of values
	/*	1 = BYTE 8-bit unsigned integer.
		2 = ASCII 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL (binary zero).
		3 = SHORT 16-bit (2-byte) unsigned integer.
		4 = LONG 32-bit (4-byte) unsigned integer.
		5 = RATIONAL 2 LONG after each other */
	int32_t count;			//the count of values, in our cases it is always 1
	int32_t offset_value;	//The pointer to value of this field, or directly the value of this field if it fits into 4 byte
} __attribute__((__packed__)) Tiff_Directory_Entry;

/****************************************
 * Minimal TIFF image file header mask. *
 ****************************************/
typedef struct {
	int16_t type; /* II for little endian*/
	int16_t reserved; /* 0 42*/
	int32_t firstIFDOffset; /* IFD should be places after image data */

	//IFD
	int16_t de_count;//always 12 in our case
	Tiff_Directory_Entry ImageWidth;
	Tiff_Directory_Entry ImageLength;
	Tiff_Directory_Entry BitsPerSample;
	Tiff_Directory_Entry Compression;
	Tiff_Directory_Entry PhotometricInterpretation;

	Tiff_Directory_Entry StripOffsets;
	Tiff_Directory_Entry SamplesPerPixel;
	Tiff_Directory_Entry RowsPerStrip;
	Tiff_Directory_Entry StripByteCounts;
	Tiff_Directory_Entry XResolution;
	Tiff_Directory_Entry YResolution;
	Tiff_Directory_Entry ResolutionUnit;
	int32_t ifd_next;//must be always 0000,

	int32_t resx;
	int32_t resx_den;

	int32_t resy;
	int32_t resy_den;

} __attribute__((__packed__)) TiffHeader;

/********************************************************************************
 * ImageData class : represents an abstract tiff image. Stores the image width, *
 * the image height and a pointer to raw image data.                            *
 * It is also able to generate a tiff file header based on parameters.          *
 ********************************************************************************/
typedef struct {
	int width;
	int height;
	char* pixeldata;
} ImageData;
/**
 * Returns the width of specified image in pixels.
 */
int getWidth(ImageData* id);
/**
 * Returns the height of specified image in pixels.
 */
int getHeight(ImageData* id);
/**
 * Returns a poiter to raw image data.
 */
char * getPixelData(ImageData* id);

void setWidth(ImageData* id,int width);
void setHeight(ImageData* id,int height);
void setPixelData(ImageData* id,char * data);
/**
 * Fills a minimal tiff file header based on image width and height.
 */
TiffHeader calculateTiffHeader(ImageData* id);
/**
 * End of ImageData class
 */


/**************************************************************
 * Tiffloader class : utility methods for loading tiff images *
 * from a byte array, and saving byte arrays as tiff images.  *
 **************************************************************/

/**
 * Interprets specified byte array as a tiff image. If everything goes
 * fine, than the specified ImageData instance will be initialized properly and returns zero.
 * <p>
 * If any error occurs, than the return value is a non-zero errorCode. Use resolveErrorCode() method
 * to get a user readable error message for a code.
 * <p>
 * Parameters:
 *  1. fileData : the tiff file content as a byte array
 *  2. size : the size of fileData buffer in bytes
 *  3. image : a non-null ImageData instance within the image metadata will be loaded.
 */
int initializeImageData(char * fileData,long size, ImageData* image);

/*
 * Saves specified ImageData instance as a tiff file in file system. Returns 0, if everything went fine, 
 * or an error code if any error happened.Use resolveErrorCode() method to get a user readable error message
 * for a code.
 * <p>
 * Parameters:
 *  1. image : a non-null ImageData instance that contains the raw image data to save.
 *  2. size : the size of raw image data
 *  3. fileName :  the file path of target file in filesystem.
 */
int saveImage(ImageData* image,long size, const char * fileName);

/**
 * Returns a character array containing a user readable message for specified error code, or NULL if unknown error code was specified.
 * <p>
 * Parameters:
 *  1. errorCode : the errorCode to resolve
 */
const char* resolveErrorCode(int errorCode);
/**
 * End of Tiffloader class
 */
