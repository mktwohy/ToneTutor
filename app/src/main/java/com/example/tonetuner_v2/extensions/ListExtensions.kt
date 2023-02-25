package com.example.tonetuner_v2.extensions

import com.example.tonetuner_v2.util.groupByIndex

fun List<Float>.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
) = this.toMutableList().apply { normalize(lowerBound, upperBound) }

fun List<Float>.normalizeBySum(): List<Float> {
    val sum = this.sum()
    return this.map { it / sum }
}

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
    if (minValue == 0f && maxValue == 0f) {
        return
    }
    if (maxValue <= upperBound && maxValue > upperBound &&
        minValue >= lowerBound && minValue < lowerBound
    ) {
        return
    }

    // Normalize
    for (i in indices) {
        this[i] = ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound
    }
}

@JvmName("sumListsFloat")
fun List<List<Float>>.sumLists(): List<Float> =
    this.groupByIndex().map { it.sum() }