#include <jni.h>
#include "nativeLibLibschrift.h"
#include "hu_qgears_textrender_libschrift_LibschriftNative.h"
#include <stdio.h>
#include <string.h>

/**
 * Converts a Java TrueTypeFont object to a native C struct
 * 
 * @param env JNI environment pointer
 * @param fontObject Java TrueTypeFont object
 * @return T_TrueTypeFont struct with converted values
 */
static T_TrueTypeFont convertJavaTrueTypeFont(JNIEnv *env, jobject fontObject);

static void throwException(JNIEnv *env, T_ErrorHandler* eh);

static void disposeTrueTypeFont(JNIEnv *env, jobject fontObject, T_TrueTypeFont* font);

/*
 * Method:    createSurfaceWithDataPrivate
 * Signature: (Ljava/nio/ByteBuffer;II)J
 */
JNIEXPORT jlong JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_createSurfaceWithDataPrivate
  (JNIEnv *env, jobject obj, jobject buffer, jint width, jint height, jint pixelformat)
{
    // Get the direct buffer address
    uint8_t* data = (uint8_t*)(*env)->GetDirectBufferAddress(env, buffer);
    T_ErrorHandler eh = {0};
    // Forward to native implementation
    uint64_t result = qls_createSurfaceWithDataPrivate(&eh,data, width, height,pixelformat);
    if (eh.code == QLS_ERROR_OK) {
        return (jlong)(uintptr_t)result;
    } else {
        throwException(env,&eh);
        return 0;
    }
}

/*
 * Method:    renderTextPrivate
 * Signature: (JLjava/lang/String;Ljava/lang/String;Lhu/qgears/images/text/EHorizontalAlign;Lhu/qgears/images/text/EVerticalAlign;IIIIFFFFZLhu/qgears/images/text/EWrapMode;)Lhu/qgears/images/SizeInt;
 */
JNIEXPORT jobject JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_renderTextPrivate
  (JNIEnv *env, jobject obj, jlong surfaceId, jobject font, jstring text, jobject hAlign, jobject vAlign, 
   jint x, jint y, jint width, jint height, jfloat r, jfloat g, jfloat b, 
   jfloat a, jboolean clip, jobject wrapMode)
{
    (void)obj;
    
    // Convert Java strings to C strings
    const jchar* c_text = (*env)->GetStringChars(env, text, 0);
    
    T_TrueTypeFont c_font = convertJavaTrueTypeFont(env,font);
    jsize length = (*env)->GetStringLength(env,text);

    // Extract enum values from Java objects
    int hAlignValue = (*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    int vAlignValue = (*env)->CallIntMethod(env, vAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, vAlign), "ordinal", "()I"));
    int wrapModeValue = (*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    
    T_ErrorHandler eh = {0};
    // Forward to native implementation
    T_SizeInt result = qls_renderTextPrivate(&eh,(uint64_t)surfaceId, &c_font, c_text,(uint32_t) length,
                                     (uint32_t)hAlignValue, (uint32_t)vAlignValue, x, y, width, height, r, g, 
                                     b, a, clip, (uint32_t)wrapModeValue);
    
    // Release the Java strings
    (*env)->ReleaseStringChars(env, text, c_text);
    disposeTrueTypeFont(env,font,&c_font);
    
    if (eh.code == QLS_ERROR_OK) {
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
    } else {

        throwException(env,&eh);
        return NULL;
    }
}



/*
 * Method:    layoutTextPrivate
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lhu/qgears/images/text/EHorizontalAlign;Lhu/qgears/images/text/EVerticalAlign;IILhu/qgears/images/text/EWrapMode;)Lhu/qgears/images/SizeInt;
 */
JNIEXPORT jobject JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_layoutTextPrivate
  (JNIEnv *env, jobject obj, jobject font, jstring text, jobject hAlign, jobject vAlign, 
   jint width, jint height, jobject wrapMode)
{
    (void)obj;
    T_ErrorHandler eh = {0};
    // Convert Java strings to C strings
    const jchar* c_text = (*env)->GetStringChars(env, text, 0);
    uint32_t textLen = (uint32_t)( (*env)->GetStringLength(env,text) );
    T_TrueTypeFont c_font = convertJavaTrueTypeFont(env,font);
    // Extract enum values from Java objects
    uint32_t wrapModeValue = (uint32_t)(*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    uint32_t hAlignValue = (uint32_t)(*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    
    // Forward to native implementation
    T_SizeInt result = qls_layoutTextPrivate(&eh,&c_font,c_text, textLen, hAlignValue, width, wrapModeValue);
    
    // Release the Java strings
    (*env)->ReleaseStringChars(env, text, c_text);
    disposeTrueTypeFont(env,font,&c_font);

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


static T_TrueTypeFont convertJavaTrueTypeFont(JNIEnv *env, jobject fontObject) {
    T_TrueTypeFont result = {0};
    
    if (fontObject == NULL) {
        return result;
    }
    
    // Get the class of the font object
    jclass fontClass = (*env)->GetObjectClass(env, fontObject);
    if (fontClass == NULL) {
        return result;
    }
    
    // Get fontFamily field (String)
    jfieldID fontFamilyField = (*env)->GetFieldID(env, fontClass, "fontFamily", "Ljava/lang/String;");
    if (fontFamilyField != NULL) {
        jstring fontFamilyString = (*env)->GetObjectField(env, fontObject, fontFamilyField);
        if (fontFamilyString != NULL) {
            // Just store the string pointer - don't allocate new memory
            result.fontFamily = (*env)->GetStringUTFChars(env, fontFamilyString, 0);
            // Note: We won't release here as we're passing this to other functions that will handle it
        }
    }
    
    // Get fontSize field (float)
    jfieldID fontSizeField = (*env)->GetFieldID(env, fontClass, "fontSize", "F");
    if (fontSizeField != NULL) {
        result.fontSize = (*env)->GetFloatField(env, fontObject, fontSizeField);
    }
    
    // Get letterSpacing field (double)
    jfieldID letterSpacingField = (*env)->GetFieldID(env, fontClass, "letterSpacing", "D");
    if (letterSpacingField != NULL) {
        result.letterSpacing = (*env)->GetDoubleField(env, fontObject, letterSpacingField);
    }
    
    // Get bold field (boolean)
    jfieldID boldField = (*env)->GetFieldID(env, fontClass, "bold", "Z");
    if (boldField != NULL) {
        result.bold = (*env)->GetBooleanField(env, fontObject, boldField);
    }
    
    // Get italic field (boolean)
    jfieldID italicField = (*env)->GetFieldID(env, fontClass, "italic", "Z");
    if (italicField != NULL) {
        result.italic = (*env)->GetBooleanField(env, fontObject, italicField);
    }
    
    // Get underline field (boolean)
    jfieldID underlineField = (*env)->GetFieldID(env, fontClass, "underline", "Z");
    if (underlineField != NULL) {
        result.underline = (*env)->GetBooleanField(env, fontObject, underlineField);
    }
    
    return result;
}

static void disposeTrueTypeFont(JNIEnv *env, jobject fontObject, T_TrueTypeFont* font) {
    if (fontObject == NULL || font == NULL || font->fontFamily == NULL) {
        return;
    }
    
    // Get the class of the font object
    jclass fontClass = (*env)->GetObjectClass(env, fontObject);
    if (fontClass == NULL) {
        return ;
    }
    // Get fontFamily field (String)
    jfieldID fontFamilyField = (*env)->GetFieldID(env, fontClass, "fontFamily", "Ljava/lang/String;");
    if (fontFamilyField != NULL) {
        jstring fontFamilyString = (*env)->GetObjectField(env, fontObject, fontFamilyField);
        if (fontFamilyString != NULL) {
             (*env)->ReleaseStringUTFChars(env, fontFamilyString, font->fontFamily);
             font->fontFamily = NULL;
        }
    }
}

const char *filename(const char *str)
{
    if (str == NULL) {
        return NULL;
    }

    const char *last = str;

    for (const char *p = str; *p != '\0'; p++) {
        if (*p == '/') {
            last = p+1;
        }
    }

    return last;  // if not found → original str
}

static void throwException(JNIEnv *env, T_ErrorHandler* eh) {
    jclass exc = (*env)->FindClass(env, "java/lang/RuntimeException");
    eh->errorMsg[QLS_MAX_ERROR_MSG_SIZE-1] = '\0';
    uint32_t len = (uint32_t)strlen(eh->errorMsg);
    if (len < QLS_MAX_ERROR_MSG_SIZE){
        char* msgPtr = &(eh->errorMsg[len]);
        if (eh->file){
            snprintf(msgPtr,QLS_MAX_ERROR_MSG_SIZE - len,
                " at %s#%d. Error code %d", 
                filename(eh->file),
                eh->line,
                eh->code);
        } else {
            snprintf(msgPtr,QLS_MAX_ERROR_MSG_SIZE - len,
                " Error code %d", 
                eh->code);
        }
    }

    if (exc != NULL) {
        (*env)->ThrowNew(env, exc, eh->errorMsg);
    }
}