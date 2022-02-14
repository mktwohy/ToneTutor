package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.HarmonicSeries
import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.Interval
import com.example.signallib.enums.Note
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.util.*
import java.util.*
import kotlin.random.Random

object SynthPitchTest{
    data class Input(
        val pitch: Float,
        val bufferSize: Int,
        val harmonicFingerprint: List<Float>,
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
        val percentError = calcError(input.pitch, output.pitch)
        val intervalError =
            if (expectedNote != null && actualNote != null) calcInterval(expectedNote, actualNote)
            else null
    }

    private fun ClosedRange<Float>.random(): Float =
        Random.nextDouble(this.start.toDouble(), this.endInclusive.toDouble()).toFloat()


    fun createRandomTestInputs(
        numTests: Int,
        frequencies: List<Float>,
        bufferSizes: List<Int>,
    ): List<Input>{
        return List(numTests){
            val harmonicSeries = HarmonicSeries(30)
            Input(
                pitch = frequencies.random(),
                bufferSize = bufferSizes.random(),
                harmonicFingerprint = harmonicSeries.apply { this.generateRandom() }.getFingerPrint()
            )
        }
    }

    fun HarmonicSeries.getFingerPrint(): List<Float> =
        List(this.numHarmonics){ this[it + 1] }
}