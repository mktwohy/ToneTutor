package com.example.tonetuner_v2.util

import android.util.Log
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun logd(message: Any){ Log.d("m_tag",message.toString()) }

fun logTime(title: String = "", block: () -> Unit){
    measureTimeMillis { block() }.also { logd("$title $it ms") }
}

fun avgTimeMillis(repeat: Int, block: () -> Unit): Float {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureTimeMillis{ block() }
            .also{ times += it }
    }
    return times.average().toFloat()
}

fun avgTimeNano(repeat: Int, block: () -> Any?): Float {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureNanoTime{ block() }
            .also{ times += it }
    }
    return times.average().toFloat()
}

fun printThreads(){
    val threads: Set<Thread> = Thread.getAllStackTraces().keys
    println("numThreads: ${threads.size}")
    for (t in threads) {
        val name = t.name
        val state = t.state
        val priority = t.priority
        val type = if (t.isDaemon) "Daemon" else "Normal"
        System.out.printf("%-20s \t %s \t %d \t %s\n", name, state, priority, type)
    }
}

fun <T, R> List<Pair<T, R>>.toPrettyString(
    indentLevel: Int = 0,
    preLine: String = "",
    postLine: String = ""
): String {
    val sb = StringBuilder()
    for ((first, second) in this){
        repeat(indentLevel){ sb.append("\t") }
        sb.append("$preLine$first: $second$postLine\n")
    }
    return sb.toString()
}