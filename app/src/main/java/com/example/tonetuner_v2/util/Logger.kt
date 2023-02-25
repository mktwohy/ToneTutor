package com.example.tonetuner_v2.util

import timber.log.Timber
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

object Logger {
    fun init() {
        Timber.plant(Timber.DebugTree())
    }

    fun d(message: String) {
        Timber.d(message)
    }

    fun i(message: String) {
        Timber.i(message)
    }

    fun w(message: String) {
        Timber.w(message)
    }

    fun e(message: String) {
        Timber.e(message)
    }

    fun e(error: Throwable) {
        Timber.e(error)
    }

    fun v(message: String) {
        Timber.v(message)
    }

    fun measureTimeMillis(title: String? = null, block: () -> Unit) {
        measureTimeMillis(block).also { d("$title $it ms") }
    }

    fun measureAvgTimeMillis(repeat: Int, title: String? = null, block: () -> Unit) {
        val measuredTimes = List(repeat) { measureTimeMillis(block) }
        val message = avgTimeMessage(title, measuredTimes.average(), "ms")
        d(message)
    }

    fun measureAvgTimeNano(repeat: Int, title: String? = null, block: () -> Unit) {
        val measuredTimes = List(repeat) { measureNanoTime(block) }
        val message = avgTimeMessage(title, measuredTimes.average(), "ns")
        d(message)
    }

    private fun avgTimeMessage(title: String?, avgTime: Double, unit: String): String =
        buildString {
            append("Measure average time")
            if (title != null) {
                append(" for $title")
            }
            append(": $avgTime $unit")
        }
}
