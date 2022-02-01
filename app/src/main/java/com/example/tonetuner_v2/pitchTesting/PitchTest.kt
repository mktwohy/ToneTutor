package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.Note
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.calcError
import com.example.tonetuner_v2.calcFreq
import com.example.tonetuner_v2.calcInterval
import com.example.tonetuner_v2.toNote
import java.util.*

class PitchTest{
    data class HarmonicSeriesSettings(
        val decayRate: Float,
        val floor: Float,
        val ceiling: Float,
        val filter: HarmonicFilter
    )

    sealed class Input{
        data class SignalInput(
            val numSamples: Int,
            val note: Note,
            val pitchBend: Float,
            val amp: Float,
            val waveShape: WaveShape,
            val harmonicSeriesSettings: HarmonicSeriesSettings,
        ): Input()
    }

    data class Output(
        val input: Input.SignalInput,
        val actualPitch: Float,
    ){
        val expectedPitch = calcFreq(input.note, (input.pitchBend * 100).toInt())
        val percentError: Float = calcError(expectedPitch, actualPitch)
        val intervalError = actualPitch.toNote()?.let { input.note.calcInterval(it) }
    }

    data class Summary(
        val averageError: Float,
        val medianError: Float,
        val percentOctaveErrors: Float
    )

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

            val hsSettings = mutableListOf<HarmonicSeriesSettings>()

            for (decayRate in decayRates){
                for (floor in floors){
                    for (ceiling in ceilings){
                        for (filter in filters){
                            hsSettings.add(
                                HarmonicSeriesSettings(decayRate, floor, ceiling, filter)
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
                                        numSamples,
                                        note,
                                        pitchBend,
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