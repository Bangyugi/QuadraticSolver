#include <jni.h>
#include <string>
#include <cmath>
#include <thread>
#include <chrono>


extern "C" {

jdoubleArray solveQuadratic(JNIEnv* env, jobject thiz, jdouble a, jdouble b, jdouble c) {
    std::this_thread::sleep_for(std::chrono::seconds(3));

    double status = 1;
    double x1 = 0.0;
    double x2 = 0.0;

    if (a == 0) {
        if (b == 0) {
            status = (c == 0) ? 0 : 1; 
        } else {
            status = 2.0;
            x1 = -c / b;
        }
    } else {
        double delta = b * b - 4 * a * c;
        if (delta < 0) {
            status = 1;
        } else if (delta == 0) {
            status = 3;
            x1 = -b / (2 * a);
            x2 = x1;
        } else {
            status = 4;
            x1 = (-b + std::sqrt(delta)) / (2 * a);
            x2 = (-b - std::sqrt(delta)) / (2 * a);
        }
    }

    jdoubleArray result = env->NewDoubleArray(3);
    if (result == nullptr) {
        return nullptr;
    }

    jdouble buf[3] = { status, x1, x2 };
    env->SetDoubleArrayRegion(result, 0, 3, buf);

    return result;
}

static JNINativeMethod methods[] = {
    {
        "solveQuadraticFromNative",
        "(DDD)[D",
        (void*)solveQuadratic
    }
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("com/example/quadraticsolver/QuadraticNativeLib");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

}