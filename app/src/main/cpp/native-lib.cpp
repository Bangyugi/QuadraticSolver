#include <jni.h>
#include <string>
#include <cmath>
#include <sstream>
#include <thread>
#include <chrono>

jstring solveQuadratic(JNIEnv* env, jobject thiz, jdouble a, jdouble b, jdouble c) {
    std::this_thread::sleep_for(std::chrono::seconds(3));
    std::stringstream result;
    if (a == 0) {
        if (b == 0) {
            result << (c == 0 ? "Phương trình vô số nghiệm" : "Phương trình vô nghiệm");
        } else {
            double x = -c / b;
            result << "Phương trình bậc 1 có 1 nghiệm:\nx = " << x;
        }
    } else {
        double delta = b * b - 4 * a * c;
        if (delta < 0) {
            result << "Phương trình vô nghiệm (Delta < 0)";
        } else if (delta == 0) {
            double x = -b / (2 * a);
            result << "Phương trình có nghiệm kép:\nx1 = x2 = " << x;
        } else {
            double x1 = (-b + std::sqrt(delta)) / (2 * a);
            double x2 = (-b - std::sqrt(delta)) / (2 * a);
            result << "Phương trình có 2 nghiệm phân biệt:\nx1 = " << x1 << "\nx2 = " << x2;
        }
    }

    return env->NewStringUTF(result.str().c_str());
}

static JNINativeMethod methods[] = {
    {
            "solveQuadraticFromNative",
            "(DDD)Ljava/lang/String;",
            (void*)solveQuadratic
    }
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("com/example/quadraticsolver/MainActivity");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}