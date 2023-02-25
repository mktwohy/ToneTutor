package com.example.tonetuner_v2.util

import android.util.Log
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun logd(message: Any) { Log.d("m_tag", message.toString()) }

fun logTime(title: String = "", block: () -> Unit) {
    measureTimeMillis { block() }.also { logd("$title $it ms") }
}

fun avgTimeMillis(repeat: Int, block: () -> Unit): Float {
    val times = mutableListOf<Long>()
    repeat(repeat) {
        measureTimeMillis { block() }
            .also { times += it }
    }
    return times.average().toFloat()
}

fun avgTimeNano(repeat: Int, block: () -> Any?): Float {
    val times = mutableListOf<Long>()
    repeat(repeat) {
        measureNanoTime { block() }
            .also { times += it }
    }
    return times.average().toFloat()
}
