package com.example.tonetuner_v2.util

import com.example.tonetuner_v2.audio.audioProcessing.Harmonic
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

fun percentage(value: Number, total: Number): Float {
    return (value.toFloat() / total.toFloat()) * 100f
}

fun calcError(expected: Number, actual: Number): Float {
    expected as Float
    actual as Float
    return ((actual - expected) / expected) * 100
}

fun <T> Collection<T>.median(): T where T : Comparable<T> {
    val midIndex = (this.size / 2f).roundToInt()
    return this.toList().sorted()[midIndex]
}

fun Int.fact() =
    (1..this).reduce { acc, i -> acc * i }

fun combinations(n: Int, k: Int) =
    n.fact() / k.fact() * (n - k).fact()

fun ClosedRange<Float>.toList(step: Float): List<Float> {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.HALF_DOWN
    val size = ((endInclusive - start) / step).roundToInt() + 1
    return List(size) { index ->
        (step * index + start).also { df.format(it) }
    }
}

fun Float.toRadian() = this * Math.PI.toFloat() / 180

fun Float.toDegree() = this * 180 / Math.PI.toFloat()

fun List<Float>.normalizeBySum(): List<Float> {
    val sum = this.sum()
    return this.map { it / sum }
}

@JvmName("sumListsFloat")
fun List<List<Float>>.sumLists(): List<Float> =
    this.groupByIndex().map { it.sum() }

fun arange(start: Float, stop: Float? = null, step: Float = 1f): List<Float> {
    val (lStart, lStop) =
        if (stop == null)
            0f to start - 1f
        else
            start to stop

    val size = ((lStop - lStart) / step).roundToInt() + 1
    return List(size) { index -> step * index + lStart }
}

fun poly(x: List<Float>, y: List<Float>): Harmonic {
    val coef = polyFit(x, y)
    val a = coef[0]
    val b = coef[1]
    val c = coef[2]

    return Harmonic(-b / (2 * a), c - b.pow(2) / (4 * a))
}

fun quadInterp(x: Float, xVals: List<Float>, yVals: List<Float>): Float {
    val coef = polyFit(xVals, yVals)
    return coef[0] * x.pow(2) + coef[1] * x + coef[2]
}

fun polyFit(x: List<Float>, y: List<Float>): List<Float> {
    val denom = (x[0] - x[1]) * (x[0] - x[2]) * (x[1] - x[2])
    val a = (x[2] * (y[1] - y[0]) + x[1] * (y[0] - y[2]) + x[0] * (y[2] - y[1])) / denom
    val b = (x[2].pow(2) * (y[0] - y[1]) + x[1].pow(2) * (y[2] - y[0]) + x[0].pow(2) * (y[1] - y[2])) / denom
    val c = (x[1] * x[2] * (x[1] - x[2]) * y[0] + x[2] * x[0] * (x[2] - x[0]) * y[1] + x[0] * x[1] * (x[0] - x[1]) * y[2]) / denom

    return listOf(a, b, c)
}

fun List<Float>.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
) = this.toMutableList().apply { normalize(lowerBound, upperBound) }

fun MutableList<Float>.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
) {
    // Check that array isn't empty
    if (isEmpty()) return

    val minValue = this.minByOrNull { it }!!
    val maxValue = this.maxByOrNull { it }!!
    val valueRange = (maxValue - minValue).toFloat()
    val boundRange = (upperBound - lowerBound).toFloat()

    // Check that array isn't already normalized
    // (I would use in range, but this produces excess memory)
    if ((minValue == 0f && maxValue == 0f) ||
        (
            maxValue <= upperBound && maxValue > upperBound &&
                minValue >= lowerBound && minValue < lowerBound
            )
    ) {
        return
    }

    // Normalize
    for (i in indices) {
        this[i] = ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound
    }
}
