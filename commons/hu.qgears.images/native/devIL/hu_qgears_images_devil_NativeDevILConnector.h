/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class hu_qgears_images_devil_NativeDevILConnector */

#ifndef _Included_hu_qgears_images_devil_NativeDevILConnector
#define _Included_hu_qgears_images_devil_NativeDevILConnector
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    initDevIL
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_initDevIL
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    bindImage
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_bindImage
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    getTypeId
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_getTypeId
  (JNIEnv *, jobject, jstring);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    loadImage
 * Signature: (Ljava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_loadImage
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    convertImage
 * Signature: ()Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_convertImage
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    getWidthPrivate
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_getWidthPrivate
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    getHeightPrivate
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_getHeightPrivate
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_init
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    saveImage
 * Signature: (Ljava/nio/ByteBuffer;Ljava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_saveImage
  (JNIEnv *, jobject, jobject, jstring, jint, jint);

/*
 * Class:     hu_qgears_images_devil_NativeDevILConnector
 * Method:    nativeDispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hu_qgears_images_devil_NativeDevILConnector_nativeDispose
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif