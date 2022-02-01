package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.Note
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.*
import java.util.*

class PitchTest{
    data class HarmonicSettings(
        val decayRate: Float,
        val floor: Float,
        val ceiling: Float,
        val filter: HarmonicFilter
    )

    private interface PitchTestInput{ val note: Note ; val pitchBend: Float ; val pitch: Float }

    sealed class Input: PitchTestInput {
        data class SignalInput(
            override val note: Note,
            override val pitchBend: Float,
            val numSamples: Int,
            val amp: Float,
            val waveShape: WaveShape,
            val harmonicSettings: HarmonicSettings,
        ): Input() {
            override val pitch = calcFreq(note, (pitchBend * 100).toInt())
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

    data class TestSummary(
        val input: Input,
        val output: Output
    ){
        val percentError: Float = calcError(input.pitch, output.pitch)
        val intervalError = output.pitch.toNote()?.let { input.note.calcInterval(it) }
    }


    companion object {
        fun allInputPermutations(
            numSamples: Int,
            notes: List<Note>,
            pitchBends: List<Float>,
            amps: List<Float>,
            waveShapes: List<WaveShape>,
            decayRates: List<Float>,
            floors: List<Float>,
            ceilings: List<Float>,
            filters: List<HarmonicFilter>
        ): LinkedList<Input> {
            // todo this is super ugly, but I don't know how else to do it

            val hsSettings = mutableListOf<HarmonicSettings>()

            for (decayRate in decayRates){
                for (floor in floors){
                    for (ceiling in ceilings){
                        for (filter in filters){
                            hsSettings.add(
                                HarmonicSettings(decayRate, floor, ceiling, filter)
                            )
                        }
                    }
                }
            }

            val tests = LinkedList<Input>()

            for (note in notes){
                for (pitchBend in pitchBends){
                    for (amp in amps){
                        for (waveShape in waveShapes){
                            for (hsUpdate in hsSettings){
                                tests.add(
                                    Input.SignalInput(
                                        note,
                                        pitchBend,
                                        numSamples,
                                        amp,
                                        waveShape,
                                        hsUpdate
                                    )
                                )
                            }
                        }
                    }
                }
            }
            return tests
        }
    }

}