
#include <jni.h>
#include <unistd.h>

extern "C" JNIEXPORT JNICALL
jint Java_app_simple_inure_services_RootService_nativeGetUid(JNIEnv *env, jobject instance) {
    return getuid();
}