// #define PNG_DEBUG 3
#include <png.h>
#include <string.h>
#include "nativeLibpng.h"
#include "hu_qgears_images_libpng_NativeLibPngConnector.h"
#define PNGCLASS Java_hu_qgears_images_libpng_NativeLibPngConnector_
#include "jniutil.h"

void clearStruct(PngImage * str)
{
	str->png_ptr=NULL;
	str->info_ptr=NULL;
	str->row_pointers=NULL;
	str->destination=NULL;
}

/**
 * Custom read function to load data from memory rather than file.
 */
static void read_data(png_structp png_ptr, png_bytep data, png_size_t length)
{
	PngImage * str=(PngImage *) png_get_io_ptr(png_ptr);
	if(str->readAt+length > str->readSize)
	{
		png_error(png_ptr, "PNG file read overflow");
		return;
	}
	memcpy(data, str->data+str->readAt, length);
	str->readAt+=length;
}

static void write_data(png_structp png_ptr, png_bytep data, png_size_t length)
{
	PngImage * str=(PngImage *)png_get_io_ptr(png_ptr);
	if(str->writeError)
	{
		return;
	}
	int sizeLimit=536870912;
	unsigned int reqSize=str->writePosition+length;
	if(length>sizeLimit||reqSize>sizeLimit)
	{
		// Overflow
		str->writeError=1;
		png_error(png_ptr, "write buffer overflow");
		return;
	}
	unsigned int size=str->writeSize;
	if(reqSize>size)
	{
		while(reqSize>size)
		{
			size*=2;
		}
		char * oldptr=str->destination;
		str->destination=(char *)realloc(oldptr, size);
		if(str->destination==NULL)
		{
			str->writeError=1;
			str->destination=oldptr;
			png_error(png_ptr, "out of memory error re-allocating memory");
		}else
		{
			str->writeSize=size;
		}
	}
	if(!str->writeError)
	{
		memcpy(str->destination+str->writePosition, data, length);
		str->writePosition+=length;
	}
}
static void write_flush(png_structp png_ptr)
{
}

/*
 This class is a wrapper for a byte array, and is responsible for auto-freeing
 it, when becomes unnecessary.
*/
class TemporaryImage {
	unsigned char * imageBuffer;
	int bufferSize_;
public:
	TemporaryImage(){
		imageBuffer = NULL;
		bufferSize_ = 0;
	}
	
	void init (int bufferSize){
		bufferSize_ = bufferSize;
		imageBuffer =(unsigned char *) malloc(sizeof (unsigned char) * bufferSize);
	}
	~TemporaryImage(){
		if(imageBuffer){
			free(imageBuffer);
			imageBuffer = NULL;
			bufferSize_ = 0;
		}
	}

	unsigned char * getImageBuffer(){
		return imageBuffer;
	}

	int getBufferSize(){
		return this->bufferSize_;	
	}
};

int clampMul(float f, int r) {
	float ret=f*(0xFF&r);
	if(ret>255)ret=255;
	if(ret<0)
	{
		ret=0;
	}
	return (int)ret;
}

/**
 * Converts images with premultiplied alpha pixel format to normal pixel
 * format (divides rgb values with alpha). The input is a byte array, the
 * output will be placed into the given TemporaryImage object. If swapAlpha
 * == true, than method assumes that alpha value comes from the first
 * channel (0), otherwise it comes from last channel (3)
 * 
 */
void convertToNormal(TemporaryImage * image, bool swapAlpha,unsigned char * original){
	unsigned char * dest = image->getImageBuffer();
	for (int i = 0 ; i< image->getBufferSize();i+=4){
		int a, r, g, b;
		int aP,rP,gP,bP;		
		if(swapAlpha){
			aP = i;
			rP = i+1; 		
			gP = i+2; 		
			bP = i+3; 		
		} else {
			rP = i;
			gP = i+1; 		
			bP = i+2; 		
			aP = i+3; 	
		}
		r = original[rP]; 		
		g = original[gP]; 		
		b = original[bP]; 	
		a = original[aP];
		float f;
		if (a == 0){
			f = 0;	
		} else {
			a = a&0xFF;
			f = 255.0 / a;		
		}		
		r = clampMul(f,r);
		g = clampMul(f,g);
		b = clampMul(f,b);
		
		dest[aP] = a;
		dest[rP] = r;
		dest[gP] = g;
		dest[bP] = b;
	}
}


METHODPREFIX(PNGCLASS, void, beginSave)(ST_ARGS, 
	jint width, jint height, jint rowBytes,
	jint nChannel,
	jboolean swapAlpha,
	jboolean swapBGR,
	jboolean premultipliedAlpha,
	jobject dataBuffer)
{
	initObj(env, obj, "ptr", sizeof(PngImage));
	MYHEAD(PngImage, ptr)
	clearStruct(str);
	str->readSize=env->GetDirectBufferCapacity(dataBuffer);
	unsigned char * bufferContent = (unsigned char *)env->GetDirectBufferAddress(dataBuffer);
	TemporaryImage tempImage; // destructor called automatically when this method ends
	
	if (premultipliedAlpha){
		tempImage.init(str->readSize);

		if (!tempImage.getImageBuffer()) {
			JNU_ThrowByName(env, "java/lang/OutOfMemoryError", "Could not "
					"allocate memory for image conversion");
			return;
		}

		convertToNormal(&tempImage,swapAlpha,bufferContent);
		str->data =tempImage.getImageBuffer();
	} else {
		str->data= bufferContent;
	}

	str->png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
	if (!str->png_ptr)
	{
		JNU_ThrowByName(env, EXCCLASS, "Can not initialize PNG struct");
		return;
	}
	if (setjmp(png_jmpbuf(str->png_ptr)))
	{
		JNU_ThrowByName(env, EXCCLASS, "Error writing PNG file");
		return;
	}
	
	str->info_ptr = png_create_info_struct(str->png_ptr);
	if (!str->info_ptr)
	{
		JNU_ThrowByName(env, EXCCLASS, "Can not initialize PNG info struct");
		return;
	}
	int defaultSize=str->readSize/10;
	if(defaultSize<1000)
	{
		defaultSize=1000;
	}
	str->destination=(char *)malloc(defaultSize);

	if (!str->destination) {
		JNU_ThrowByName(env, "java/lang/OutOfMemoryError", "Could not allocate "
				"memory when saving image");
		return;
	}

	str->writeSize=defaultSize;
	str->writePosition=0;
	str->writeError=0;
	
	png_set_write_fn(str->png_ptr, str, write_data, write_flush);
	int color_type;
	switch(nChannel)
	{
		case 1:
			color_type=PNG_COLOR_TYPE_GRAY;
			break;
		case 3:
			color_type=PNG_COLOR_TYPE_RGB;
			break;
		case 4:
			color_type=PNG_COLOR_TYPE_RGB_ALPHA;
			break;
		default:
			JNU_ThrowByName(env, EXCCLASS, 255, "Unknown image nchannel type: %d", nChannel);
			return;
	}
	png_set_IHDR(str->png_ptr, str->info_ptr, width, height,
		8, color_type, PNG_INTERLACE_NONE,
		PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);
	if(swapAlpha)
	{
		png_set_swap_alpha(str->png_ptr);
	}
	if(swapBGR)
	{
		png_set_bgr(str->png_ptr);
	}
	png_write_info(str->png_ptr, str->info_ptr);
	if(str->readSize<height*rowBytes)
	{
		JNU_ThrowByName(env, EXCCLASS, "Image buffer is smaller than should be");
		return;
	}
	str->row_pointers = (png_bytep*) malloc(sizeof(png_bytep) * height);

	if (!str->row_pointers) {
		JNU_ThrowByName(env, "java/lang/OutOfMemoryError", "Could not "
				"allocate memory for row pointers when saving image");

		// Freeing previously successfully allocated area
		if (str->destination) {
			free(str->destination);
		}

		return;
	}

	for (int y=0; y<height; y++)
	{
		str->row_pointers[y] = (png_byte*) str->data+rowBytes*y;
	}
	png_write_image(str->png_ptr, str->row_pointers);
	
	png_write_end(str->png_ptr, NULL);
	
	if(str->writeError)
	{
		JNU_ThrowByName(env, EXCCLASS, "PNG file output buffering error");
		return;
	}
	return;
}
METHODPREFIX(PNGCLASS, jint, getFileSize)(ST_ARGS)
{
	MYHEAD(PngImage, ptr)
	return str->writePosition;
}
METHODPREFIX(PNGCLASS, void, saveImage)(ST_ARGS, jobject file)
{
	MYHEAD(PngImage, ptr)
	
	char * data=(char *)env->GetDirectBufferAddress(file);
	jlong size=env->GetDirectBufferCapacity(file);
	if(str->writePosition!=size)
	{
		JNU_ThrowByName(env, EXCCLASS, "Invalid data buffer size");
		return;
	}
	
	memcpy(data, str->destination, size);
}
METHODPREFIX(PNGCLASS, void, closeSave)(ST_ARGS)
{
	MYHEAD(PngImage, ptr)
	png_destroy_write_struct(&(str->png_ptr), &(str->info_ptr));
	if(str->row_pointers!=NULL)
	{
		free(str->row_pointers);
	}
	if(str->destination!=NULL)
	{
		free(str->destination);
	}
	clearStruct(str);
	free(str);
}

METHODPREFIX(PNGCLASS, void, beginLoad)(ST_ARGS, jobject dataBuffer)
{
	initObj(env, obj, "ptr", sizeof(PngImage));
	MYHEAD(PngImage, ptr)
	clearStruct(str);
	str->data=(unsigned char *)env->GetDirectBufferAddress(dataBuffer);
	str->readSize=env->GetDirectBufferCapacity(dataBuffer);
	if (png_sig_cmp(str->data, 0, 8))
	{
		JNU_ThrowByName(env, EXCCLASS, "Invalid PNG image header");
		return;
	}
	str->png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
	if (!str->png_ptr)
	{
		JNU_ThrowByName(env, EXCCLASS, "png_create_read_struct failed");
		return;
	}
	if (setjmp(png_jmpbuf(str->png_ptr)))
	{
		JNU_ThrowByName(env, EXCCLASS, "Error during loading PNG");
		return;
	}
	str->info_ptr = png_create_info_struct(str->png_ptr);
	if (!str->info_ptr)
	{
		JNU_ThrowByName(env, EXCCLASS, "png_create_info_struct failed");
		return;
	}
	png_set_read_fn(str->png_ptr, str, read_data);
	png_set_sig_bytes(str->png_ptr, 0);

	png_read_info(str->png_ptr, str->info_ptr);

//	str->colorType = png_get_color_type(png_ptr, info_ptr);
//	str->bit_depth = png_get_bit_depth(png_ptr, info_ptr);
	
	png_set_expand(str->png_ptr);	// Expand to 8 bit resolution if data has less
	png_set_strip_16(str->png_ptr);	// Strip to 8 bit resolution if data has more 
	/*
	    PNG_TRANSFORM_IDENTITY      No transformation
    PNG_TRANSFORM_STRIP_16      Strip 16-bit samples to
                                8 bits
    PNG_TRANSFORM_STRIP_ALPHA   Discard the alpha channel
    PNG_TRANSFORM_PACKING       Expand 1, 2 and 4-bit
                                samples to bytes
    PNG_TRANSFORM_PACKSWAP      Change order of packed
                                pixels to LSB first
    PNG_TRANSFORM_EXPAND        Perform set_expand()
    PNG_TRANSFORM_INVERT_MONO   Invert monochrome images
    PNG_TRANSFORM_SHIFT         Normalize pixels to the
                                sBIT depth
    PNG_TRANSFORM_BGR           Flip RGB to BGR, RGBA
                                to BGRA
    PNG_TRANSFORM_SWAP_ALPHA    Flip RGBA to ARGB or GA
                                to AG
    PNG_TRANSFORM_INVERT_ALPHA  Change alpha from opacity
                                to transparency
    PNG_TRANSFORM_SWAP_ENDIAN
    */
	
	
	png_set_interlace_handling(str->png_ptr);
	// Update png info structure with the requested transformations
	png_read_update_info(str->png_ptr, str->info_ptr);
	
	int type=png_get_color_type(str->png_ptr, str->info_ptr);
	switch(type)
	{
		case PNG_COLOR_TYPE_GRAY:
			str->nChannel=1;
			break;
		case PNG_COLOR_TYPE_RGB:
			str->nChannel=3;
			break;
		case PNG_COLOR_TYPE_RGB_ALPHA:
			str->nChannel=4;
			break;
		default:
			JNU_ThrowByName(env, EXCCLASS, 255, "Unknown image color type: %d", type);
			return;
	}
	str->width = png_get_image_width(str->png_ptr, str->info_ptr);
	str->height = png_get_image_height(str->png_ptr, str->info_ptr);
	str->rowBytes=png_get_rowbytes(str->png_ptr, str->info_ptr);
}
METHODPREFIX(PNGCLASS, void, loadImage)(ST_ARGS, jobject pixelData, jint rowBytes)
{
	MYHEAD(PngImage, ptr)
	char * raw_ptr=(char *)env->GetDirectBufferAddress(pixelData);
	jlong size=env->GetDirectBufferCapacity(pixelData);
	if(size<rowBytes*str->height)
	{
		JNU_ThrowByName(env, EXCCLASS, 255, "PNG read error: allocated pixel "
				"buffer is too small: %d", (int)size);
		return;
	}
	str->row_pointers = (png_bytep*) malloc(sizeof(png_bytep) * str->height);

	if (!str->row_pointers) {
		JNU_ThrowByName(env, "java/lang/OutOfMemoryError", "Could not "
				"allocate memory for row pointers when loading image");

		return;
	}

	for (int y=0; y<str->height; y++)
	{
		str->row_pointers[y] = (png_byte*) raw_ptr+rowBytes*y;
	}
	if (setjmp(png_jmpbuf(str->png_ptr)))
	{
		JNU_ThrowByName(env, EXCCLASS, "Error during loading PNG data");
		return;
	}
	png_read_image(str->png_ptr, str->row_pointers);
}
METHODPREFIX(PNGCLASS, void, closeLoad)(ST_ARGS)
{
	MYHEAD(PngImage, ptr)
	png_destroy_read_struct(&(str->png_ptr), &(str->info_ptr), NULL);
	if(str->row_pointers!=NULL)
	{
		free(str->row_pointers);
	}
	clearStruct(str);
	free(str);
}
METHODPREFIX(PNGCLASS, jint, getWidth)(ST_ARGS)
{
	MYHEAD(PngImage, ptr)
	return str->width;
}
METHODPREFIX(PNGCLASS, jint, getHeight)(ST_ARGS)
{
	MYHEAD(PngImage, ptr)
	return str->height;
}
METHODPREFIX(PNGCLASS, jint, getRowBytes)(ST_ARGS)
{
	MYHEAD(PngImage, ptr)
	return str->rowBytes;
}
METHODPREFIX(PNGCLASS, jint, getNumberOfChannels)(ST_ARGS)
{
	MYHEAD(PngImage, ptr)
	return str->nChannel;
}

