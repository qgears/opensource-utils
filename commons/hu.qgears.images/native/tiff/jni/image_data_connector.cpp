#include "hu_qgears_images_tiff_ImageData.h"
#define CLASS Java_hu_qgears_images_tiff_ImageData_
#define EXCCLASS "hu/qgears/images/tiff/NativeTiffLoaderException"
#include "jniutil.h"
#include "../tiffloader.h"

METHODPREFIX(CLASS, void, init)(ST_ARGS)
{
	initObj(env, obj, "ptr", sizeof(ImageData));
}
METHODPREFIX(CLASS, jint, getWidth)(ST_ARGS)
{
	MYHEAD(ImageData,ptr)
	return getWidth(str);

}
METHODPREFIX(CLASS, jint, getHeight)(ST_ARGS)
{
	MYHEAD(ImageData,ptr)
	return getHeight(str);

}
METHODPREFIX(CLASS, jint, getPixelDataOffset)(JNIEnv * env, jclass clazz)
{
	return sizeof(TiffHeader);
}

METHODPREFIX(CLASS, void, disposePrimitive)(ST_ARGS)
{
	disposeObj(env, obj, "ptr");
}
