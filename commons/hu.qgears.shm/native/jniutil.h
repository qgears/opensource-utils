#include <string.h>
#include <errno.h>
#define METHODPREFIX(JNICLASS, PARAMTYPE, METHODNAME) METHODPREFIX_INTERNAL(JNICLASS, PARAMTYPE, METHODNAME)
#define METHODPREFIX_INTERNAL(JNICLASS, PARAMTYPE, METHODNAME) JNIEXPORT PARAMTYPE JNICALL \
	JNICLASS ## METHODNAME
#define ST_ARGS JNIEnv * env, jobject obj

jfieldID getLongFieldId(ST_ARGS, const char * fieldName);
void * getStruct(ST_ARGS, const char * fieldName);
void * getStructByFID(ST_ARGS, jfieldID fieldId);
void initObj(ST_ARGS, const char * fieldName, int size);

void disposeObj(ST_ARGS, const char * fieldName);
int copyStringInput(ST_ARGS, jstring str, char * out, int maxLengthWithZeroTerm, const char * excType, const char * excMsg);

void JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg);
void JNU_ThrowByNameErrno(JNIEnv *env, const char *name, const char *msg, int err);


#define MYHEADID(JNISTRUCT, FIELDID) MYHEAD_INTERNALID(JNISTRUCT, FIELDID)
#define MYHEAD_INTERNALID(JNISTRUCT, FIELDID) JNISTRUCT * str=(JNISTRUCT *)getStructByFID(env, obj, FIELDID );
#define MYHEAD(JNISTRUCT, FIELDNAME) MYHEAD_INTERNAL(JNISTRUCT, FIELDNAME)
#define MYHEAD_INTERNAL(JNISTRUCT, FIELDNAME) JNISTRUCT * str=(JNISTRUCT *)getStruct(env, obj, #FIELDNAME );

