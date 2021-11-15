package com.example.tonetuner_v2

import kotlin.math.pow

// Should I store this as an enum class?


/** The pitch (fundamental frequency) of the audio sample */
val twmPitchAlgo: (List<Harmonic>) -> Double = { harmonics ->
    // Generate function for comparing different fundamental frequencies
    val calcScores = twmScore(harmonics)

    // todo what do each of these steps do?
    val pass1 = arange(-29.0, 7.0)      // generate range from -29.0..7.0
        .map { 440 * 2.0.pow(it / 12) }         // ????
        .map { Harmonic(it, calcScores(it)) }       //
        .minByOrNull { it.mag }?.freq as Double

    val n = 12 * Math.log(pass1 / 440.0) / Math.log(2.0)

    // return
    arange(n - 1, n + 1, 0.1)
        .map { 440 * 2.0.pow(it / 12) }
        .map { Harmonic(it, calcScores(it)) }
        .minByOrNull { it.mag }?.freq as Double
}

val testAlgo: (List<Harmonic>) -> Double = {
    harmonics -> 440.0
}