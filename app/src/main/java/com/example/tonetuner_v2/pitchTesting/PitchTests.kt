package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.HarmonicSeries
import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.plus
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.calcError
import com.example.tonetuner_v2.calcFreq
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import kotlin.math.absoluteValue

class PitchTest{
    sealed class Input{
        data class SignalInput(
            val numSamples: Int,
            val note: Note,
            val pitchBend: Float,
            val amp: Float,
            val waveShape: WaveShape,
            val updateHarmonicSeries: (HarmonicSeries) -> Unit,
        ): Input() {
            val expectedPitch = calcFreq(note, (pitchBend * 100).toInt())
        }
    }

    data class Results(
        val test: Input.SignalInput,
        val actualPitch: Float,
    ){
        val error: Float = calcError(test.expectedPitch, actualPitch)
    }

    companion object {
        fun createPitchTests(
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

            val harmonicSeriesUpdates = mutableListOf<(HarmonicSeries) -> Unit>()

            for (decayRate in decayRates){
                for (floor in floors){
                    for (ceiling in ceilings){
                        for (filter in filters){
                            harmonicSeriesUpdates.add { h: HarmonicSeries ->
                                h.generate(decayRate, floor, ceiling, filter.function)
                            }
                        }
                    }
                }
            }

            val tests = LinkedList<PitchTest.Input>()

            for (note in notes){
                for (pitchBend in pitchBends){
                    for (amp in amps){
                        for (waveShape in waveShapes){
                            for (hsUpdate in harmonicSeriesUpdates){
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




//private fun initPitchTests(): MutableList<PitchTest> {
//    val tests = mutableListOf<PitchTest>()
//
//    var note        = Note.A_4
//    var pitchBend   = 0f
//    var amp         = 1f
//    var waveShape   = WaveShape.SINE
//    var updateHarmonicSeries = { h: HarmonicSeries ->
//        h.generate(
//            decayRate = 15f,
//            floor = 0f,
//            ceiling = 1f,
//            filter = HarmonicFilter.ALL.function
//        )
//    }
//
//    fun addTestForEachNote(notes: Collection<Note> = Note.notes){
//        for(n in notes){
//            tests.add(
//                SignalPitchTest(
//                    note        = n,
//                    pitchBend   = pitchBend,
//                    amp         = amp,
//                    waveShape   = waveShape,
//                    updateHarmonicSeries = updateHarmonicSeries
//                )
//            )
//        }
//    }
//
//
//    addTestForEachNote()
//
//    return tests
//
//
//}