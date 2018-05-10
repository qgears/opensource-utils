#include <stdlib.h>

#include <jni.h>
#include "jniutil.h"
jfieldID getLongFieldId(ST_ARGS, const char * fieldName)
{
	jclass clazz=env->GetObjectClass(obj);
	jfieldID fieldId=env->GetFieldID(clazz, fieldName, "J");
	env->DeleteLocalRef(clazz);
	return fieldId;
}

void * getStruct(ST_ARGS, const char * fieldName)
{
	jfieldID fieldId=getLongFieldId(env, obj, fieldName);
	jlong ptr=env->GetLongField(obj, fieldId);
	return (void *)ptr;
}
void initObj(ST_ARGS, const char * fieldName, int size)
{
	jfieldID fieldId=getLongFieldId(env, obj, fieldName);
	void * str=(void *)calloc(size, 1);
	env->SetLongField(obj, fieldId, (jlong)str);
}

void disposeObj(ST_ARGS, const char * fieldName)
{
 void * ptr=getStruct(env, obj, fieldName);
 free(ptr);
}


void JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
	jclass cls = env->FindClass(name);
	/* if cls is NULL, an exception has already been thrown */
	if (cls != NULL)
	{
		env->ThrowNew(cls, msg);
	}
	/* free the local ref */
	env->DeleteLocalRef(cls);
}


