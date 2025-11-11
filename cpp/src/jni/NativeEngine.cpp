#include <jni.h>
#include "core/Calculator.h"
#include "velox/SubstraitPlanProcessor.h"
#include <vector>

extern "C" {

JNIEXPORT jint JNICALL 
Java_org_opensearch_sql_NativeEngine_nativeAdd(JNIEnv *env, jclass clazz, jint a, jint b) {
    return Calculator::add(a, b);
}

JNIEXPORT jint JNICALL 
Java_org_opensearch_sql_NativeEngine_nativeMultiply(JNIEnv *env, jclass clazz, jint a, jint b) {
    return Calculator::multiply(a, b);
}

JNIEXPORT void JNICALL 
Java_org_opensearch_sql_NativeEngine_nativeCallJava(JNIEnv *env, jobject obj) {
    Calculator::callJavaMethod(env, obj);
}

JNIEXPORT jstring JNICALL 
Java_org_opensearch_sql_NativeEngine_printSubstraitPlan(JNIEnv *env, jclass clazz, jbyteArray planBytes) {
    jsize len = env->GetArrayLength(planBytes);
    jbyte* bytes = env->GetByteArrayElements(planBytes, nullptr);
    
    std::vector<uint8_t> planData(reinterpret_cast<uint8_t*>(bytes), 
                                  reinterpret_cast<uint8_t*>(bytes) + len);
    
    std::string jsonResult = SubstraitPlanProcessor::processPlanFromBytes(planData);
    
    env->ReleaseByteArrayElements(planBytes, bytes, JNI_ABORT);
    
    return env->NewStringUTF(jsonResult.c_str());
}

}
