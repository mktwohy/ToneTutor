package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.HarmonicSeries
import com.example.signallib.enums.Note
import com.example.tonetuner_v2.util.*
import kotlin.random.Random

object SynthPitchTest{
    data class Input(
        val pitch: Float,
        val bufferSize: Int,
        val harmonicFingerprint: Fingerprint,
    ){
        val note: Note?
        val cents: Int?
        init {
            val (n, c) = pitch.toNoteAndCents()
            note = n
            cents = c
        }
    }

    data class Output(
        val pitch: Float,
    ){
        val note: Note?
        val cents: Int?
        init {
            val (n, c) = pitch.toNoteAndCents()
            note = n
            cents = c
        }
    }

    class CsvCase(
        input: Input,
        output: Output
    ){
        val expectedPitch = input.pitch
        val expectedNote  = input.note
        val expectedCents = input.cents
        val actualPitch = output.pitch
        val actualNote = output.note
        val actualCents = output.cents
        val bufferSize = input.bufferSize
        val fingerprint = input.harmonicFingerprint
        val fingerprintSize = input.harmonicFingerprint.harmonicSeries.numHarmonics
        val percentError = calcError(input.pitch, output.pitch)
        val intervalError =
            if (expectedNote != null && actualNote != null) calcInterval(expectedNote, actualNote)
            else null
    }

    fun createRandomTestInputs(
        numTests: Int,
        frequencies: List<Float>,
        bufferSizes: List<Int>,
        fingerPrints: List<Fingerprint>
    ): List<Input>{
        return List(numTests){
            Input(
                pitch = frequencies.random(),
                bufferSize = bufferSizes.random(),
                harmonicFingerprint = fingerPrints.random()
            )
        }
    }

    fun HarmonicSeries.getFingerPrint(): List<Float> =
        List(this.numHarmonics){ this[it + 1] }
}