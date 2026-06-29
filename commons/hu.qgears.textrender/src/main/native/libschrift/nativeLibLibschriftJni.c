#include <jni.h>
#include "nativeLibLibschrift.h"
#include "hu_qgears_textrender_libschrift_LibschriftNative.h"

/*
 * Method:    createSurfaceWithDataPrivate
 * Signature: (Ljava/nio/ByteBuffer;II)J
 */
JNIEXPORT jlong JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_createSurfaceWithDataPrivate
  (JNIEnv *env, jobject obj, jobject buffer, jint width, jint height)
{
    // Get the direct buffer address
    uint8_t* data = (uint8_t*)(*env)->GetDirectBufferAddress(env, buffer);
    (void)obj;
    
    // Forward to native implementation
    uint64_t result = qls_createSurfaceWithDataPrivate(data, width, height);
    return (jlong)(uintptr_t)result;
}

/*
 * Method:    renderTextPrivate
 * Signature: (JLjava/lang/String;Ljava/lang/String;Lhu/qgears/images/text/EHorizontalAlign;Lhu/qgears/images/text/EVerticalAlign;IIIIFFFFZLhu/qgears/images/text/EWrapMode;)Lhu/qgears/images/SizeInt;
 */
JNIEXPORT jobject JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_renderTextPrivate
  (JNIEnv *env, jobject obj, jlong surfaceId, jstring fontPath, jstring text, jobject hAlign, jobject vAlign, 
   jint x, jint y, jint width, jint height, jfloat fontSize, jfloat letterSpacing, jfloat lineSpacing, 
   jfloat wordSpacing, jboolean useKerning, jobject wrapMode)
{
    (void)obj;
    
    // Convert Java strings to C strings
    const jchar* c_text = (*env)->GetStringChars(env, text, 0);
    const char* c_fontPath = (*env)->GetStringUTFChars(env, fontPath, 0);
    jsize length = (*env)->GetStringLength(env,text);

    // Extract enum values from Java objects
    int hAlignValue = (*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    int vAlignValue = (*env)->CallIntMethod(env, vAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, vAlign), "ordinal", "()I"));
    int wrapModeValue = (*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    
    // Forward to native implementation
T_SizeInt result = qls_renderTextPrivate((uint64_t)surfaceId, c_fontPath, c_text,(uint32_t) length,
                                     (uint32_t)hAlignValue, (uint32_t)vAlignValue, x, y, width, height, fontSize, letterSpacing, 
                                     lineSpacing, wordSpacing, useKerning, (uint32_t)wrapModeValue);
    
    // Release the Java strings
    (*env)->ReleaseStringChars(env, text, c_text);
    (*env)->ReleaseStringUTFChars(env, fontPath, c_fontPath);
    
    // Create and return SizeInt object from T_SizeInt result
    jclass sizeIntClass = (*env)->FindClass(env, "hu/qgears/images/SizeInt");
    if (sizeIntClass == NULL) {
        return NULL;
    }
    
    jmethodID constructor = (*env)->GetMethodID(env, sizeIntClass, "<init>", "(II)V");
    if (constructor == NULL) {
        return NULL;
    }
    
    // Return SizeInt object with width and height from the C struct
    return (*env)->NewObject(env, sizeIntClass, constructor, result.width, result.height);
}

/*
 * Method:    layoutTextPrivate
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lhu/qgears/images/text/EHorizontalAlign;Lhu/qgears/images/text/EVerticalAlign;IILhu/qgears/images/text/EWrapMode;)Lhu/qgears/images/SizeInt;
 */
JNIEXPORT jobject JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_layoutTextPrivate
  (JNIEnv *env, jobject obj, jstring text, jstring fontPath, jobject hAlign, jobject vAlign, 
   jint width, jint height, jobject wrapMode)
{
    (void)env;
    (void)obj;
    
    // Convert Java strings to C strings
    const jchar* c_text = (*env)->GetStringChars(env, text, 0);
    const char* c_fontPath = (*env)->GetStringUTFChars(env, fontPath, 0);
    
    // Extract enum values from Java objects
    int hAlignValue = (*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    int vAlignValue = (*env)->CallIntMethod(env, vAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, vAlign), "ordinal", "()I"));
    int wrapModeValue = (*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    
    // Forward to native implementation
    T_SizeInt result = qls_layoutTextPrivate(c_fontPath,c_text, (uint32_t)hAlignValue, (uint32_t)vAlignValue, width, height, (uint32_t)wrapModeValue);
    
    // Release the Java strings
    (*env)->ReleaseStringChars(env, text, c_text);
    (*env)->ReleaseStringUTFChars(env, fontPath, c_fontPath);
    
    // Create and return SizeInt object from T_SizeInt result
    jclass sizeIntClass = (*env)->FindClass(env, "hu/qgears/images/SizeInt");
    if (sizeIntClass == NULL) {
        return NULL;
    }
    
    jmethodID constructor = (*env)->GetMethodID(env, sizeIntClass, "<init>", "(II)V");
    if (constructor == NULL) {
        return NULL;
    }
    
    // Return SizeInt object with width and height from the C struct
    return (*env)->NewObject(env, sizeIntClass, constructor, result.width, result.height);
}

/*
 * Method:    disposeSurfacePrivate
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_disposeSurfacePrivate
  (JNIEnv *env, jobject obj, jlong surfaceId)
{
    (void)env;
    (void)obj;
    
    // Forward to native implementation
    qls_disposeSurfacePrivate((uint64_t)surfaceId);
}
