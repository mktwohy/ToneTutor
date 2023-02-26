package com.example.tonetuner_v2.util

import android.content.Context

/**
 * Utility object to store a reference to the [Context] belonging to the current app process.
 */
object ContextHolder {
    private lateinit var applicationContext: Context

    fun hold(context: Context) {
        if (!::applicationContext.isInitialized) {
            synchronized(this) {
                applicationContext = context.applicationContext
            }
        }
    }

    fun get() = applicationContext
}