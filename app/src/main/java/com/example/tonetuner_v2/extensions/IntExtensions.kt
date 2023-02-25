package com.example.tonetuner_v2.extensions

fun Int.factorial() =
    (1..this).reduce { acc, i -> acc * i }
