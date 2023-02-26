package com.example.tonetuner_v2.audio.audioProcessing

import com.example.tonetuner_v2.app.AppSettings
import com.example.tonetuner_v2.extensions.mapToIndices
import com.example.tonetuner_v2.extensions.normalizeBySum
import com.example.tonetuner_v2.util.freqToPitch

data class Harmonic(var freq: Float, var mag: Float)

fun List<List<Harmonic>>.averageLists() =
    this
        .flatten()
        .groupBy { it.freq }
        .map { (freq, harmonics) ->
            Harmonic(freq, harmonics.map { it.mag }.average().toFloat())
        }

fun List<Harmonic>.assignMagsToIndices(size: Int): List<Float> {
    if (this.isEmpty()) return listOf()

    val ret = FloatArray(size)
    val indexToValue = this.map { it.freq.toInt() to it.mag }
//    val size = indexToValue.maxOf { it.first } + 1
    for ((index, value) in indexToValue) {
        if (index < size)
            ret[index] = value
    }
    return ret.toList()
}

fun List<Harmonic>.toGraphRepr() =
    this.asSequence()
        .onEach { it.freq = freqToPitch(it.freq) } // convert frequencies to pitch
        .map { it.freq.toInt() to it.mag } // convert pitches to Int indices
        .groupBy { it.first } // group by index
        .filter { it.key >= 0 } // filter positive indices
        .map { (index, harmonics) -> // map so output is List<index to max freq>
            index to harmonics.maxOf { it.second }
        }
        .mapToIndices(0f)
        .normalizeBySum()

fun List<Harmonic>.toFingerPrint(): List<Float> {
    val f = this.associate { it.freq.toInt() to it.mag }
    return List(AppSettings.FINGERPRINT_SIZE) { i -> f[i] ?: 0f }
}

fun List<List<Harmonic>>.sumLists(): List<Harmonic> =
    when (size) {
        0 -> listOf()
        1 -> this[0]
        else ->
            this
                .flatten()
                .groupBy { it.freq }
                .map { group ->
                    Harmonic(
                        freq = group.key,
                        mag = group.value.map { it.mag }.sum()
                    )
                }
    }
