#include <jni.h>
#include "nativeLibLibschrift.h"
#include "hu_qgears_textrender_libschrift_LibschriftNative.h"
#include "hu_qgears_textrender_TrueTypeFont.h"
#include "util.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

static void throwException(JNIEnv *env, T_ErrorHandler* eh);
static void dispose_native_font(T_ErrorHandler* eh, JNIEnv *env, jobject fontObject);
static T_TrueTypeFont* get_or_create_native_font(T_ErrorHandler * eh, JNIEnv *env, jobject fontObject);
static jfieldID get_field_id(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName, const char* signature);
static double get_double_field(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName);
static float get_float_field(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName);
static jstring get_string_field(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName);

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
    
    jsize length = (*env)->GetStringLength(env,text);
    
    // Extract enum values from Java objects
    int hAlignValue = (*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    int vAlignValue = (*env)->CallIntMethod(env, vAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, vAlign), "ordinal", "()I"));
    int wrapModeValue = (*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    
    T_ErrorHandler eh = {0};
    T_TrueTypeFont* c_font = get_or_create_native_font(&eh,env,font);
    T_SizeInt result = {0};
    if (eh.code == QLS_ERROR_OK)
    {
        // Forward to native implementation
        result = qls_renderTextPrivate(&eh,(uint64_t)surfaceId, c_font, c_text,(uint32_t) length,
                                         (uint32_t)hAlignValue, (uint32_t)vAlignValue, x, y, width, height, SCHRIFT_F32_from_float(r), SCHRIFT_F32_from_float(g), 
                                         SCHRIFT_F32_from_float(b), SCHRIFT_F32_from_float(a), clip, (uint32_t)wrapModeValue);
    }
    
    // Release the Java strings
    (*env)->ReleaseStringChars(env, text, c_text);
    
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
    // Extract enum values from Java objects
    uint32_t wrapModeValue = (uint32_t)(*env)->CallIntMethod(env, wrapMode, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, wrapMode), "ordinal", "()I"));
    uint32_t hAlignValue = (uint32_t)(*env)->CallIntMethod(env, hAlign, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, hAlign), "ordinal", "()I"));
    
    T_TrueTypeFont* c_font = get_or_create_native_font(&eh,env,font);
    T_SizeInt result;
    if (eh.code == QLS_ERROR_OK)
    {
        // Forward to native implementation
        result = qls_layoutTextPrivate(&eh,c_font,c_text, textLen, hAlignValue, width, wrapModeValue);
    }
    
    // Release the Java strings
    (*env)->ReleaseStringChars(env, text, c_text);
    
    if (eh.code != QLS_ERROR_OK)
    {
        throwException(env,&eh);
        return NULL;
    }
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

static jfieldID get_field_id(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName, const char* signature) {
    if (object == NULL) {
        ERROR(eh, QLS_ERROR_INVALID_FONT_OBJ, "Object is null while accessing field '%s'", fieldName);
        return NULL;
    }
    jclass objectClass = (*env)->GetObjectClass(env, object);
    if (objectClass == NULL) {
        ERROR(eh, QLS_ERROR_INVALID_FONT_OBJ, "Class not found while accessing field '%s'", fieldName);
        return NULL;
    }
    jfieldID fieldId = (*env)->GetFieldID(env, objectClass, fieldName, signature);
    if (fieldId == NULL) {
        // GetFieldID leaves a pending NoSuchFieldError that must not reach Java code
        (*env)->ExceptionClear(env);
        ERROR(eh, QLS_ERROR_INVALID_FONT_OBJ, "Field '%s' with signature '%s' is not found", fieldName, signature);
    }
    (*env)->DeleteLocalRef(env, objectClass);
    return fieldId;
}

static double get_double_field(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName) {
    jfieldID fieldId = get_field_id(eh, env, object, fieldName, "D");
    if (fieldId == NULL) {
        return 0.0;
    }
    return (double)(*env)->GetDoubleField(env, object, fieldId);
}
static float get_float_field(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName) {
    jfieldID fieldId = get_field_id(eh, env, object, fieldName, "F");
    if (fieldId == NULL) {
        return 0.0;
    }
    return (float)(*env)->GetFloatField(env, object, fieldId);
}
static jstring get_string_field(T_ErrorHandler * eh, JNIEnv *env, jobject object, const char* fieldName) {
    jfieldID fieldId = get_field_id(eh, env, object, fieldName, "Ljava/lang/String;");
    if (fieldId == NULL) {
        return NULL;
    }
    return (jstring)(*env)->GetObjectField(env, object, fieldId);
}

static T_TrueTypeFont* get_or_create_native_font(T_ErrorHandler * eh, JNIEnv *env, jobject fontObject)
{
    T_TrueTypeFont* font;

    jfieldID nativePtrFieldId = get_field_id(eh,env,fontObject,"nativePtr","J");
    if (eh->code == QLS_ERROR_OK)
    {
        font = (T_TrueTypeFont*) ((*env)->GetLongField(env, fontObject, nativePtrFieldId));
        if (!font)
        {
            font = (T_TrueTypeFont*) malloc(sizeof(T_TrueTypeFont));
            (*env)->SetLongField(env, fontObject, nativePtrFieldId,(jlong)font);
            memset(font,0, sizeof(T_TrueTypeFont));
            font->letterSpacing = SCHRIFT_F64_from_double( get_double_field(eh,env,fontObject, "letterSpacing") );
            if (eh->code == QLS_ERROR_OK)
            {
                jstring ttfFilePathString = get_string_field(eh,env,fontObject, "ttfFilePath");
                if (eh->code == QLS_ERROR_OK)
                {
                    // Just store the string pointer - don't allocate new memory
                    font->ttfFilePath = (*env)->GetStringUTFChars(env, ttfFilePathString, 0);
                }
            }
            if (eh->code == QLS_ERROR_OK)
            {
                font->fontSize = SCHRIFT_F32_from_float(get_float_field(eh,env,fontObject,"fontSize"));
            }
        }
    }
    return font;
}

static void dispose_native_font(T_ErrorHandler* eh, JNIEnv *env, jobject fontObject)
{
    T_TrueTypeFont* font;
    jfieldID nativePtrFieldId = get_field_id(eh,env,fontObject,"nativePtr","J");
    if (eh->code == QLS_ERROR_OK)
    {
        font = (T_TrueTypeFont*) ((*env)->GetLongField(env, fontObject, nativePtrFieldId));
        if (font)
        {
            (*env)->SetLongField(env, fontObject, nativePtrFieldId,0);
            jstring ttfFilePathString = get_string_field(eh,env,fontObject, "ttfFilePath");
            if (eh->code == QLS_ERROR_OK)
            {
                (*env)->ReleaseStringUTFChars(env, ttfFilePathString, font->ttfFilePath);
                font->ttfFilePath = NULL;
            }
            if (font->font)
            {
                sft_freefont(font->font);
                font->font = NULL;
            }
        }
    }
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

JNIEXPORT void JNICALL Java_hu_qgears_textrender_libschrift_LibschriftNative_clearSurfacePrivate
  (JNIEnv * env, jobject obj, jlong surfaceHandle)
{
	qls_clearSurfacePrivate((uint64_t)surfaceHandle);
}


JNIEXPORT void JNICALL Java_hu_qgears_textrender_TrueTypeFont_nativeDispose
  (JNIEnv *env, jobject fontObject)
{
    T_ErrorHandler eh = {0};
    dispose_native_font(&eh,env,fontObject);
    if (eh.code != QLS_ERROR_OK)
    {
        throwException(env,&eh);
    }
}
