//
// Created by macropreprocessor on 8/06/19.
//

#include <jni.h>
#include "system/core/libutils/include/utils/Log.h" // ALOGE()
#include <stdlib.h> // abort()

namespace JVM {
    JavaVM* jvm;
    JNIEnv* GetEnv() {
        void* env = NULL;
        jint status = jvm->GetEnv(&env, JNI_VERSION_1_6);
        if ((env != NULL) && status != JNI_OK) {
            ALOGE("ERROR: GetEnv failed");
            abort();
        }
        return reinterpret_cast<JNIEnv*>(env);
    }
};

