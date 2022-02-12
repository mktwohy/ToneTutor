package com.example.tonetuner_v2.util

import androidx.compose.ui.graphics.Color
import java.util.concurrent.BlockingQueue


fun <T> List<Pair<Int, T>>.mapToIndices(defaultValue: T): List<T> =
    this.toMap()
        .let { indexToValue ->
            val maxIndex = indexToValue.maxOfOrNull { it.key } ?: 0
            MutableList(maxIndex + 1){ indexToValue[it] ?: defaultValue }
        }

/**
 * like toString(), but it returns a substring a desired length. The end is padded with
 * zeros if needed.
 */
fun Float.toString(length: Int) =
    this.toString().padEnd(length, '0').substring(0, length)

/** Similar to offer(), but, if there is no space, it removes an element to make room */
fun <T> BlockingQueue<T>.forcedOffer(element: T){
    if(remainingCapacity() == 0) poll()
    offer(element)
}

fun <T> BlockingQueue<T>.clearAndOffer(element: T){
    clear()
    offer(element)
}

fun <T> List<List<T>>.elementsAtIndex(index: Int): List<T> {
    val elements = mutableListOf<T>()
    for(list in this){
        if (index in list.indices) elements += list[index]
    }
    return elements
}

fun <T> List<List<T>>.groupByIndex() =
    if (this.isEmpty())
        listOf()
    else
        List(this.maxOf { it.size } ){ this.elementsAtIndex(it) }



operator fun Color.plus(that: Color) =
    Color(
        this.red/2 + that.red/2,
        this.green/2 + that.green/2,
        this.blue/2 + that.blue/2,
        this.alpha/2 + that.alpha/2
    )








