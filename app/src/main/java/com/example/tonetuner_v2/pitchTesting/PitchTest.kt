package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.Note
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.util.*
import java.util.*
import kotlin.random.Random

object PitchTest{
    data class HarmonicSettings(
        val decayRate: Float,
        val floor: Float,
        val ceiling: Float,
        val filter: HarmonicFilter
    )

    private interface InputInterface{ val note: Note ; val cents: Int ; val pitch: Float }

    sealed class Input: InputInterface {
        data class SignalInput(
            override val note: Note,
            override val cents: Int,
            val numSamples: Int,
            val amp: Float,
            val waveShape: WaveShape,
            val harmonicSettings: HarmonicSettings,
        ): Input() {
            override val pitch = calcFreq(note, cents)
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
        val expectedNote    = input.note
        val expectedCents   = input.cents
        val expectedPitch   = input.pitch
        val actualNote      = output.note
        val actualCents     = output.cents
        val actualPitch     = output.pitch
        val percentError    = calcError(input.pitch, output.pitch)
        val intervalError   = output.pitch.toNote()?.let { input.note.calcInterval(it) }
        val numSamples: Int?
        val amp: Float?
        val waveShape: WaveShape?
        val harmonicDecayRate: Float?
        val harmonicFloor: Float?
        val harmonicCeiling: Float?
        val harmonicFilter: HarmonicFilter?

        init {

            if (input is Input.SignalInput){
                numSamples          = input.numSamples
                amp                 = input.amp
                waveShape           = input.waveShape
                harmonicDecayRate   = input.harmonicSettings.decayRate
                harmonicFloor       = input.harmonicSettings.floor
                harmonicCeiling     = input.harmonicSettings.ceiling
                harmonicFilter      = input.harmonicSettings.filter
            }
            else {
                numSamples          = null
                amp                 = null
                waveShape           = null
                harmonicDecayRate   = null
                harmonicFloor       = null
                harmonicCeiling     = null
                harmonicFilter      = null
            }
        }
    }

    fun createTestInputPermutations(
        numSamples: Int,
        notes: List<Note>,
        cents: List<Int>,
        amps: List<Float>,
        waveShapes: List<WaveShape>,
        decayRates: List<Float>,
        floors: List<Float>,
        ceilings: List<Float>,
        filters: List<HarmonicFilter>
    ): List<Input> {
        // todo this is super ugly, but I don't know how else to do it

        val harmonicSettings = mutableListOf<HarmonicSettings>()

        for (decayRate in decayRates){
            for (floor in floors){
                for (ceiling in ceilings){
                    for (filter in filters){
                        harmonicSettings.add(
                            HarmonicSettings(decayRate, floor, ceiling, filter)
                        )
                    }
                }
            }
        }

        val tests = LinkedList<Input>()

        for (note in notes){
            for (numCents in cents){
                for (amp in amps){
                    for (waveShape in waveShapes){
                        for (harmSettings in harmonicSettings){
                            tests.add(
                                Input.SignalInput(
                                    note = note,
                                    cents = numCents,
                                    numSamples = numSamples,
                                    amp = amp,
                                    waveShape = waveShape,
                                    harmonicSettings = harmSettings
                                )
                            )
                        }
                    }
                }
            }
        }
        return tests
    }

    private fun ClosedRange<Float>.random(): Float =
        Random.nextDouble(this.start.toDouble(), this.endInclusive.toDouble()).toFloat()


    fun createRandomTestInputs(
        numTests: Int,
        numSamples: IntRange,
        notes: List<Note>,
        cents: IntRange,
        amps: ClosedRange<Float>,
        waveShapes: List<WaveShape>,
        decayRates: ClosedRange<Float>,
        floors: ClosedRange<Float>,
        ceilings: ClosedRange<Float>,
        filters: List<HarmonicFilter>
    ): List<Input>{
        return List(numTests){
            Input.SignalInput(
                numSamples = numSamples.random(),
                note = notes.random(),
                cents = cents.random(),
                amp = amps.random(),
                waveShape = waveShapes.random(),
                harmonicSettings = HarmonicSettings(
                    decayRate = decayRates.random(),
                    floor = floors.random(),
                    ceiling = ceilings.random(),
                    filter = filters.random()
                )
            )
        }
    }

}