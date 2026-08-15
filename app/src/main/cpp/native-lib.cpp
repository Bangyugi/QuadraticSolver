#include <jni.h>
#include <cmath>
#include <vector>
#include <limits>

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_example_quadraticsolver_MainActivity_solveQuadraticFromNative(
        JNIEnv *env,
        jobject,
        jdouble a,
        jdouble b,
        jdouble c) {

    std::vector<double> roots;
    const double EPS = 1e-9;

    if (std::abs(a) < EPS) {
        if (std::abs(b) < EPS) {
            if (std::abs(c) < EPS) 
                roots.push_back(std::numeric_limits<double>::infinity());
            }
        } else {
            roots.push_back(-c / b);
        }
    } else {
        double delta = b * b - 4.0 * a * c;

        if (delta > EPS) {
            double sqrt_delta = std::sqrt(delta);
            roots.push_back((-b + sqrt_delta) / (2.0 * a));
            roots.push_back((-b - sqrt_delta) / (2.0 * a));
        } else if (std::abs(delta) <= EPS) {
            roots.push_back(-b / (2.0 * a));
        }

    }

    jsize size = static_cast<jsize>(roots.size());
    jdoubleArray result = env->NewDoubleArray(size);
    if (result != nullptr && size > 0) {
        env->SetDoubleArrayRegion(result, 0, size, roots.data());
    }

    return result;
}
