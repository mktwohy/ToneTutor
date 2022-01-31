package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.plus
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import java.lang.StringBuilder
import kotlin.math.absoluteValue

fun calcFreq(note: Note, cents: Int): Float {
    val sign = if (cents > 0) 1 else -1
    val noteNeighbor = note + sign
    val centsAsHz = sign * ((note.freq - noteNeighbor.freq) * cents / 100).absoluteValue
    return note.freq + centsAsHz
}

fun calcError(expected: Number, actual: Number): Float {
    expected as Float
    actual as Float
    return ((actual - expected) / expected) * 100
}

fun Collection<PitchTestResults>.toJson(): String {
    val sb = StringBuilder()
    sb.append("{\n")
    this.forEach {
        when (it.test){
            is PitchTest.SignalPitchTest -> {
                val expectedPitch = calcFreq(it.test.note, (it.test.pitchBend * 100).toInt())
                val actualPitch = it.pitch
                val error = calcError(expectedPitch, actualPitch)
                println("expected: $expectedPitch actual: $actualPitch, error: $error")
            }
        }
    }


    return sb.toString()
}

