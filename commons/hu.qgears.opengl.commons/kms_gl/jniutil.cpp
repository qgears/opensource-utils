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
void * getStructByFID(ST_ARGS, jfieldID fieldId)
{
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
void JNU_ThrowByNameErrno(JNIEnv *env, const char *name, const char *msg, int err)
{
	char tmpmsg[1024];
	tmpmsg[1024]=0;
	tmpmsg[512]=0;
	strncpy(tmpmsg, msg, 512);
	char * ptr=tmpmsg+strlen(tmpmsg);
	strcat(tmpmsg, " ");
	strncat(tmpmsg, strerror(err), 510);
	JNU_ThrowByName(env, name, tmpmsg);
}

int copyStringInput(ST_ARGS, jstring str, char * out, int maxLengthWithZeroTerm, const char * excType, const char * excMsg)
{
	const char * name = env->GetStringUTFChars(str, NULL);
	if (name == NULL) {
		return -1; // OutOfMemoryError already thrown
	}
	if(strlen(name)>maxLengthWithZeroTerm-1)
	{
		JNU_ThrowByName(env, excType, excMsg);
		return -2;
	}
	strcpy(out, name);
	env->ReleaseStringUTFChars(str, name);
	return 0;
}

