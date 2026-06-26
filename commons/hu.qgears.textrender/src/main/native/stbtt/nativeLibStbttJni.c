#include <jni.h>
#include "nativeLibStbtt.h"
#include "hu_qgears_textrender_stbtt_StbTrueTypeNative.h"

/*
 * Method:    createSurfaceWithDataPrivate
 * Signature: (Ljava/nio/ByteBuffer;II)I
 */
JNIEXPORT jint JNICALL Java_hu_qgears_textrender_stbtt_StbTrueTypeNative_createSurfaceWithDataPrivate
  (JNIEnv *env, jobject obj, jobject buffer, jint width, jint height)
{
    // Get the direct buffer address
    uint8_t* data = (uint8_t*)(*env)->GetDirectBufferAddress(env, buffer);
    
    // Forward to native implementation
    return qstb_createSurfaceWithDataPrivate(data, width, height);
}

/*
 * Method:    renderTextPrivate
 * Signature: (ILjava/lang/String;Ljava/lang/String;Lhu/qgears/images/text/EHorizontalAlign;Lhu/qgears/images/text/EVerticalAlign;IIIIFFFFZLhu/qgears/images/text/EWrapMode;)Lhu/qgears/images/SizeInt;
 */
JNIEXPORT jobject JNICALL Java_hu_qgears_textrender_stbtt_StbTrueTypeNative_renderTextPrivate
  (JNIEnv *env, jobject obj, jint surfaceId, jstring text, jstring fontPath, jobject hAlign, jobject vAlign, 
   jint x, jint y, jint width, jint height, jfloat fontSize, jfloat letterSpacing, jfloat lineSpacing, 
   jfloat wordSpacing, jboolean useKerning, jobject wrapMode)
{
    // Convert Java strings to C strings
    const char* c_text = (*env)->GetStringUTFChars(env, text, 0);
    const char* c_fontPath = (*env)->GetStringUTFChars(env, fontPath, 0);
    
    // Extract enum values from Java objects
    int hAlignValue = (*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    int vAlignValue = (*env)->CallIntMethod(env, vAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, vAlign), "ordinal", "()I"));
    int wrapModeValue = (*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    
    // Forward to native implementation
    T_SizeInt result = qstb_renderTextPrivate(surfaceId, c_text, c_fontPath, 
                                         hAlignValue, vAlignValue, x, y, width, height, fontSize, letterSpacing, 
                                         lineSpacing, wordSpacing, useKerning, wrapModeValue);
    
    // Release the Java strings
    (*env)->ReleaseStringUTFChars(env, text, c_text);
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
JNIEXPORT jobject JNICALL Java_hu_qgears_textrender_stbtt_StbTrueTypeNative_layoutTextPrivate
  (JNIEnv *env, jobject obj, jstring text, jstring fontPath, jobject hAlign, jobject vAlign, 
   jint width, jint height, jobject wrapMode)
{
    // Convert Java strings to C strings
    const char* c_text = (*env)->GetStringUTFChars(env, text, 0);
    const char* c_fontPath = (*env)->GetStringUTFChars(env, fontPath, 0);
    
    // Extract enum values from Java objects
    int hAlignValue = (*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    int vAlignValue = (*env)->CallIntMethod(env, vAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, vAlign), "ordinal", "()I"));
    int wrapModeValue = (*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    
    // Forward to native implementation
    T_SizeInt result = qstb_layoutTextPrivate(c_text, c_fontPath, hAlignValue, vAlignValue, width, height, wrapModeValue);
    
    // Release the Java strings
    (*env)->ReleaseStringUTFChars(env, text, c_text);
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
