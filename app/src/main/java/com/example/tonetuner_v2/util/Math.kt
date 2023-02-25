package com.example.tonetuner_v2.util

import com.example.tonetuner_v2.audio.audioProcessing.Harmonic
import com.example.tonetuner_v2.extensions.factorial
import kotlin.math.pow

fun percentage(value: Number, total: Number): Float =
    (value.toFloat() / total.toFloat()) * 100f

fun calcError(expected: Number, actual: Number): Float {
    expected as Float
    actual as Float
    return ((actual - expected) / expected) * 100
}

fun combinations(n: Int, k: Int) =
    n.factorial() / k.factorial() * (n - k).factorial()

fun poly(x: List<Float>, y: List<Float>): Harmonic {
    val (a, b, c) = polyFit(x, y)
    return Harmonic(
        freq = -b / (2 * a),
        mag = c - b.pow(2) / (4 * a)
    )
}

fun quadInterp(x: Float, xAxis: List<Float>, yAxis: List<Float>): Float {
    val (a, b, c) = polyFit(xAxis, yAxis)
    return a * x.pow(2) + b * x + c
}

fun polyFit(x: List<Float>, y: List<Float>): List<Float> {
    val denom = (x[0] - x[1]) * (x[0] - x[2]) * (x[1] - x[2])
    val a = (x[2] * (y[1] - y[0]) + x[1] * (y[0] - y[2]) + x[0] * (y[2] - y[1])) / denom
    val b = (x[2].pow(2) * (y[0] - y[1]) + x[1].pow(2) * (y[2] - y[0]) + x[0].pow(2) * (y[1] - y[2])) / denom
    val c = (x[1] * x[2] * (x[1] - x[2]) * y[0] + x[2] * x[0] * (x[2] - x[0]) * y[1] + x[0] * x[1] * (x[0] - x[1]) * y[2]) / denom

    return listOf(a, b, c)
}

@JvmInline
value class Degree(val value: Float) {
    fun toRadians(): Radian =
        (value * Math.PI.toFloat() / 180).radians

    override fun toString(): String =
        "$value°"
}

@JvmInline
value class Radian(val value: Float) {
    fun toDegrees(): Degree =
        (value * 180 / Math.PI.toFloat()).degrees

    override fun toString(): String =
        "$value rad"
}

val Float.degrees: Degree
    get() = Degree(this)

val Float.radians: Radian
    get() = Radian(this)
