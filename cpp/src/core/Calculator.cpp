#include "Calculator.h"
#include <jni.h>
#include <iostream>

int Calculator::add(int a, int b) {
    return a + b;
}

int Calculator::multiply(int a, int b) {
    return a * b;
}

void Calculator::callJavaMethod(void* env, void* obj) {
    JNIEnv* jenv = static_cast<JNIEnv*>(env);
    jobject jobj = static_cast<jobject>(obj);
    
    jclass clazz = jenv->GetObjectClass(jobj);
    
    jmethodID methodId = jenv->GetMethodID(clazz, "onNativeCallback", "(Ljava/lang/String;)V");
    
    if (methodId != nullptr) {
        jstring message = jenv->NewStringUTF("Hello from C++!");
        
        jenv->CallVoidMethod(jobj, methodId, message);
        
        jenv->DeleteLocalRef(message);
    }
    
    jenv->DeleteLocalRef(clazz);
}
