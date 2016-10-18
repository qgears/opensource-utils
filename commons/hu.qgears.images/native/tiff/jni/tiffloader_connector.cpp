#include "hu_qgears_images_tiff_NativeTiffLoaderConnector.h"
#define CLASS Java_hu_qgears_images_tiff_NativeTiffLoaderConnector_
#define EXCCLASS "hu/qgears/images/tiff/NativeTiffLoaderException"
#include "jniutil.h"
#include "../tiffloader.h"

METHODPREFIX(CLASS, void, saveImageAsTiffPrimitive)(ST_ARGS,jint width,jint height, jobject pdata, jstring filePath)
{

	char * data=(char *)env->GetDirectBufferAddress(pdata);
	jlong size=env->GetDirectBufferCapacity(pdata);

	ImageData image;
	setWidth(&image,width);
	setHeight(&image,height);
	setPixelData(&image,data);

	const char * fPath = env->GetStringUTFChars(filePath, NULL);

	int errorCode = saveImage(&image,size,fPath);
	env->ReleaseStringUTFChars(filePath, fPath);
	if(errorCode != 0){
		JNU_ThrowByName(env, EXCCLASS, resolveErrorCode(errorCode));
	}
}

METHODPREFIX(CLASS,void,loadTiffImagePrimitive)(ST_ARGS, jobject fileData, jobject iData){
	//std::cout<<"P1"<<std::endl;
	char * fData=(char *)env->GetDirectBufferAddress(fileData);
	jlong size=env->GetDirectBufferCapacity(fileData);

	ImageData * image = (ImageData*)getStruct(env, iData, "ptr");
	int errorCode = initializeImageData(fData,size, image);
	if(errorCode != 0){
		JNU_ThrowByName(env, EXCCLASS, resolveErrorCode(errorCode));
	}
}
