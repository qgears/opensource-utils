#include "nativeDevil.h"
#include "hu_qgears_images_devil_NativeDevIL.h"
#define DEVILCLASS Java_hu_qgears_images_devil_NativeDevIL_
#include "jniutil.h"

// Callback functions for allocation and deallocation
typedef void* (ILAPIENTRY *mAlloc)(const ILsizei);
typedef void  (ILAPIENTRY *mFree) (const void* CONST_RESTRICT);
int myCtr=0;
void * myAlloc(const ILsizei size)
{
	myCtr++;
	printf("Allocated: %d %lu\n", myCtr, size);
	fflush(stdout);
	return malloc(size);
}
void myFree(const void * ptr)
{
	myCtr--;
	printf("Freed %d\n", myCtr);
	fflush(stdout);
	free((void *)ptr);
}

METHODPREFIX(DEVILCLASS, void, initDevIL)(ST_ARGS)
{
//	ilSetMemory(myAlloc, myFree);
	ilInit();
}
METHODPREFIX(DEVILCLASS, void, init)(ST_ARGS)
{
    initObj(env, obj, "ptr", sizeof(DevILImage));
    MYHEAD(DevILImage, ptr)
    str->name = (ILuint)-1;
    str->name=ilGenImage(); /* Generation of one image name */
}
/*
METHODPREFIX(DEVILCLASS, jobject, loadImage)(ST_ARGS, jobject data, jstring ext)
{
    MYHEAD(DevILImage, ptr)
    ilBindImage(str->name);
    const char * name = env->GetStringUTFChars(ext, NULL);
    if (str == NULL) {
        return NULL; /* OutOfMemoryError already thrown
    }
    ILenum type =ilTypeFromExt(name);
    env->ReleaseStringUTFChars(ext, name);

    void * dataPtr=env->GetDirectBufferAddress(data);
    jlong size=env->GetDirectBufferCapacity(data);
    ilLoadL(type, dataPtr, size);
}
*/
METHODPREFIX(DEVILCLASS, jobject, convertImage)(ST_ARGS)
{
    MYHEAD(DevILImage, ptr)
		if(ilConvertImage(IL_BGRA, IL_UNSIGNED_BYTE))
		{
	    str->width = ilGetInteger(IL_IMAGE_WIDTH);
  	  str->height = ilGetInteger(IL_IMAGE_HEIGHT);
  	  str->data = (char *)ilGetData();
  	  int retsize= ilGetInteger(IL_IMAGE_SIZE_OF_DATA);
  	  jobject ret=env->NewDirectByteBuffer(str->data, retsize);
  	  return ret;
		}
		else
		{
			char msg[256];
			sprintf(msg, "Error converting image to BGRA - %d", ilGetError());
			JNU_ThrowByName(env, EXCCLASS, msg);
		}
}
METHODPREFIX(DEVILCLASS, jint, loadImage)(ST_ARGS, jobject data, jint typeId)
{
    MYHEAD(DevILImage, ptr)

    void * dataPtr=env->GetDirectBufferAddress(data);
    jlong size=env->GetDirectBufferCapacity(data);
    if(ilLoadL(typeId, dataPtr, size))
		{
			return 0;
		}
		return ilGetError();
}
METHODPREFIX(DEVILCLASS, jint, bindImage)(ST_ARGS)
{
    MYHEAD(DevILImage, ptr)
    ilBindImage(str->name);
    return 0;
}
METHODPREFIX(DEVILCLASS, jint, getTypeId)(ST_ARGS, jstring ext)
{
    MYHEAD(DevILImage, ptr)
    ilBindImage(str->name);
    const char * name = env->GetStringUTFChars(ext, NULL);
    if (str == NULL) {
        return 0; /* OutOfMemoryError already thrown */
    }
    ILenum type =ilTypeFromExt(name);
    env->ReleaseStringUTFChars(ext, name);
		return type;
}

METHODPREFIX(DEVILCLASS, void, saveImage)(ST_ARGS, jobject data, jstring ext, jint width, jint height)
{
    MYHEAD(DevILImage, ptr)
    ilBindImage(str->name);

    const char * name = env->GetStringUTFChars(ext, NULL);
    if (str == NULL) {
        return; // OutOfMemoryError already thrown
    }
    ILenum type=IL_PNG;
    void * dataPtr=env->GetDirectBufferAddress(data);
    jlong size=env->GetDirectBufferCapacity(data);
		ilLoadDataL(dataPtr, size, width, height, 1, 4);
		ilEnable(IL_FILE_OVERWRITE);
    ilSave(type, name);
    env->ReleaseStringUTFChars(ext, name);
    return;
}
METHODPREFIX(DEVILCLASS, jint, getWidthPrivate)(ST_ARGS)
{
	MYHEAD(DevILImage, ptr)
	return str->width;
}
METHODPREFIX(DEVILCLASS, jint, getHeightPrivate)(ST_ARGS)
{
	MYHEAD(DevILImage, ptr)
	return str->height;
}
METHODPREFIX(DEVILCLASS, void, nativeDispose)(ST_ARGS)
{
	MYHEAD(DevILImage, ptr)
	fflush(stdout);
	if((str->name)!=-1)
	{
    ilBindImage(str->name);
		ilResetMemory();
		ilDeleteImage(str->name);
		fflush(stdout);
		str->name=-1;
	}
	disposeObj(env, obj, "ptr");
}

