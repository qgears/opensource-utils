/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class hu_qgears_opengl_osmesa_OSMesaNative */

#ifndef _Included_hu_qgears_opengl_osmesa_OSMesaNative
#define _Included_hu_qgears_opengl_osmesa_OSMesaNative
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     hu_qgears_opengl_osmesa_OSMesaNative
 * Method:    createContext
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_hu_qgears_opengl_osmesa_OSMesaNative_createContext
  (JNIEnv *, jobject, jint);

/*
 * Class:     hu_qgears_opengl_osmesa_OSMesaNative
 * Method:    makeCurrentPrivate
 * Signature: (Ljava/nio/ByteBuffer;II)V
 */
JNIEXPORT void JNICALL Java_hu_qgears_opengl_osmesa_OSMesaNative_makeCurrentPrivate
  (JNIEnv *, jobject, jobject, jint, jint);

/*
 * Class:     hu_qgears_opengl_osmesa_OSMesaNative
 * Method:    disposeContext
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hu_qgears_opengl_osmesa_OSMesaNative_disposeContext
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_opengl_osmesa_OSMesaNative
 * Method:    getGlVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_hu_qgears_opengl_osmesa_OSMesaNative_getGlVersion
  (JNIEnv *, jobject);

/*
 * Class:     hu_qgears_opengl_osmesa_OSMesaNative
 * Method:    checkOsMesaLoadable
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hu_qgears_opengl_osmesa_OSMesaNative_checkOsMesaLoadable
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
