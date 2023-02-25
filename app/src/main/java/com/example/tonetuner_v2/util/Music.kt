package com.example.tonetuner_v2.util

import com.example.signallib.enums.Interval
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.minus
import com.example.signallib.enums.Note.Companion.plus
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log

fun Note.calcInterval(other: Note): Interval {
    if (this == other) return Interval.PER_1

    val distance = abs(Note.notes.indexOf(this) - Note.notes.indexOf(other))
    val index = (distance % 12).let { if (it == 0) 12 else it } - 1
    return Interval.values()[index]
}

fun calcFreq(note: Note, cents: Int): Float {
    val sign = if (cents > 0) 1 else -1
    val noteNeighbor = note + sign
    val centsAsHz = sign * ((note.freq - noteNeighbor.freq) * cents / 100).absoluteValue
    return note.freq + centsAsHz
}

/** from https://psychology.wikia.org/wiki/Pitch_perception */
fun freqToPitch(freq: Float) = 69 + 12 * log(freq / 440f, 2f)

// todo this shouldn't be an extension function
/** Converts frequency to closest note estimate*/
fun Float.toNote(): Note? {
    // check if frequency is out of bounds
    if (this < Note.C_0.freq || this > Note.B_8.freq) return null

    // find the upper estimate for the note
    var upperEst = Note.Cs0
    while (upperEst.freq < this && upperEst != Note.B_8) {
        upperEst += 1
    }

    // get the lower estimate for the note
    val lowerEst = upperEst - 1

    val upperErr = abs(upperEst.freq - this)
    val lowerErr = abs(lowerEst.freq - this)

    return if (upperErr < lowerErr) upperEst else lowerEst
}

/** Converts frequency to the closest note and its error (cents) */
fun Float.toNoteAndCents(): Pair<Note?, Int> {
    val note = this.toNote() ?: return Pair(null, 0)
    val hzError = this - note.freq

    val centsError =
        if (hzError > 0) {
            val hzToNextNote = (note + 1).freq - note.freq
            (100 * hzError / hzToNextNote).toInt()
        } else {
            if (note == Note.C_0) { 0 } else {
                val hzToPrevNote = note.freq - (note - 1).freq
                (100 * hzError / hzToPrevNote).toInt()
            }
        }
    return Pair(note, centsError)
}
