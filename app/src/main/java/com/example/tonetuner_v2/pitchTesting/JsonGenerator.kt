package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.plus
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
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


// https://stackabuse.com/reading-and-writing-json-in-kotlin-with-jackson/
fun Collection<PitchTestResults>.toJson(): String {
    val sb = StringBuilder()
    val jsonMapper = jacksonObjectMapper()

    this.forEach {
        sb.append(jsonMapper.writeValueAsString(it))
    }


    return sb.toString()
}

