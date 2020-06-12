/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class hu_qgears_opengl_kms_KMSNative */

#ifndef _Included_hu_qgears_opengl_kms_KMSNative
#define _Included_hu_qgears_opengl_kms_KMSNative
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     hu_qgears_opengl_kms_KMSNative
 * Method:    init
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_opengl_kms_KMSNative_init
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_opengl_kms_KMSNative
 * Method:    swapBuffers
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_opengl_kms_KMSNative_swapBuffers
  (JNIEnv *, jobject, jint);

/*
 * Class:     hu_qgears_opengl_kms_KMSNative
 * Method:    getCurrentBackBufferPtr
 * Signature: (I)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_hu_qgears_opengl_kms_KMSNative_getCurrentBackBufferPtr
  (JNIEnv *, jobject, jint);

/*
 * Class:     hu_qgears_opengl_kms_KMSNative
 * Method:    dispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hu_qgears_opengl_kms_KMSNative_dispose
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
