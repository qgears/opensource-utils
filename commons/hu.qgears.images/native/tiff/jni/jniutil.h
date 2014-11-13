#define METHODPREFIX(JNICLASS, PARAMTYPE, METHODNAME) METHODPREFIX_INTERNAL(JNICLASS, PARAMTYPE, METHODNAME)
#define METHODPREFIX_INTERNAL(JNICLASS, PARAMTYPE, METHODNAME) JNIEXPORT PARAMTYPE JNICALL \
	JNICLASS ## METHODNAME
#define ST_ARGS JNIEnv * env, jobject obj

#define __SOURCE_LOCATION__ __FILE__":"__LINE__

jfieldID getLongFieldId(ST_ARGS, const char * fieldName);
void * getStruct(ST_ARGS, const char * fieldName);
void initObj(ST_ARGS, const char * fieldName, int size);

void disposeObj(ST_ARGS, const char * fieldName);


void JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg);


#define MYHEAD(JNISTRUCT, FIELDNAME) MYHEAD_INTERNAL(JNISTRUCT, FIELDNAME)
#define MYHEAD_INTERNAL(JNISTRUCT, FIELDNAME) JNISTRUCT * str=(JNISTRUCT *)getStruct(env, obj, #FIELDNAME );

