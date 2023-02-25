package com.example.tonetuner_v2.extensions

/**
 * like toString(), but it returns a substring a desired length. The end is padded with
 * zeros if needed.
 */
fun Float.toString(length: Int) =
    this.toString().padEnd(length, '0').substring(0, length)
