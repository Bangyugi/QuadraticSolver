package com.example.quadraticsolver

enum class RootType {
    INFINITE_ROOTS,
    NO_REAL_ROOTS,
    ONE_REAL_ROOT,
    DOUBLE_ROOT,
    TWO_REAL_ROOTS
}

data class QuadraticResult(
    val rootType: RootType,
    val x1: Double = 0.0,
    val x2: Double = 0.0
)

object QuadraticNativeLib {
    init {
        System.loadLibrary("quadraticsolver")
    }

    external fun solveQuadraticFromNative(a: Double, b: Double, c: Double): DoubleArray

    fun solveQuadratic(a: Double, b: Double, c: Double): QuadraticResult {
        val res = solveQuadraticFromNative(a, b, c)
        val rootType = when (res.getOrNull(0)?.toInt() ?: 1) {
            0 -> RootType.INFINITE_ROOTS
            1 -> RootType.NO_REAL_ROOTS
            2 -> RootType.ONE_REAL_ROOT
            3 -> RootType.DOUBLE_ROOT
            4 -> RootType.TWO_REAL_ROOTS
            else -> RootType.NO_REAL_ROOTS
        }
        val x1 = res.getOrNull(1) ?: 0.0
        val x2 = res.getOrNull(2) ?: 0.0
        return QuadraticResult(rootType, x1, x2)
    }
}
