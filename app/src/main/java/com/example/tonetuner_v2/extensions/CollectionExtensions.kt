package com.example.tonetuner_v2.extensions

import kotlin.math.roundToInt

fun <T> Collection<T>.median(): T where T : Comparable<T> {
    val midIndex = (this.size / 2f).roundToInt()
    return this.toList().sorted()[midIndex]
}
