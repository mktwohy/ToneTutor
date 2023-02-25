package com.example.tonetuner_v2.util

import com.example.signallib.enums.Interval
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.minus
import com.example.signallib.enums.Note.Companion.plus
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log
import kotlin.math.sign

fun Note.calcInterval(other: Note): Interval {
    if (this == other) return Interval.PER_1

    val distance = abs(Note.notes.indexOf(this) - Note.notes.indexOf(other))
    val index = (distance % 12).let { if (it == 0) 12 else it } - 1
    return Interval.values()[index]
}

fun calcFreq(note: Note, cents: Int): Float {
    val noteNeighbor = if (cents > 0) note + 1 else note - 1
    val centsAsHz = cents.sign * ((note.freq - noteNeighbor.freq) * cents / 100).absoluteValue
    return note.freq + centsAsHz
}

/** from https://psychology.wikia.org/wiki/Pitch_perception */
fun freqToPitch(freq: Float): Float =
    69 + 12 * log(freq / 440f, 2f)

fun freqToClosestNote(frequency: Float): Note? {
    // check if frequency is out of bounds
    if (frequency < Note.C_0.freq || frequency > Note.B_8.freq) return null

    // find the upper estimate for the note
    var upperEst = Note.Cs0
    while (upperEst.freq < frequency && upperEst != Note.B_8) {
        upperEst += 1
    }

    // get the lower estimate for the note
    val lowerEst = upperEst - 1

    val upperErr = abs(upperEst.freq - frequency)
    val lowerErr = abs(lowerEst.freq - frequency)

    return if (upperErr < lowerErr) upperEst else lowerEst
}

fun freqToNoteAndCents(frequency: Float): Pair<Note?, Int> {
    val note = freqToClosestNote(frequency) ?: return Pair(null, 0)
    val hzError = frequency - note.freq
    val centsError = when {
        hzError > 0 -> {
            val hzToNextNote = (note + 1).freq - note.freq
            (100 * hzError / hzToNextNote).toInt()
        }
        note == Note.C_0 -> {
            0
        }
        else -> {
            val hzToPrevNote = note.freq - (note - 1).freq
            (100 * hzError / hzToPrevNote).toInt()
        }
    }
    return Pair(note, centsError)
}
