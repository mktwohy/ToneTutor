package com.example.tonetuner_v2.extensions

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

fun <T> List<List<T>>.elementsAtIndex(index: Int): List<T> {
    val elements = mutableListOf<T>()
    for (list in this) {
        if (index in list.indices) elements += list[index]
    }
    return elements
}

fun <T> List<List<T>>.groupByIndex() =
    if (this.isEmpty())
        listOf()
    else
        List(this.maxOf { it.size }) { this.elementsAtIndex(it) }

@JvmName("sumListsFloat")
fun List<List<Float>>.sumLists(): List<Float> =
    this.groupByIndex().map { it.sum() }

fun <T> List<Pair<Int, T>>.mapToIndices(defaultValue: T): List<T> =
    this.toMap()
        .let { indexToValue ->
            val maxIndex = indexToValue.maxOfOrNull { it.key } ?: 0
            MutableList(maxIndex + 1) { indexToValue[it] ?: defaultValue }
        }
