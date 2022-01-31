package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.HarmonicSeries
import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.plus
import com.example.signallib.enums.WaveShape
import java.util.*
import kotlin.math.absoluteValue


sealed class PitchTest{
    data class SignalPitchTest(
        val numSamples: Int,
        val note: Note,
        val pitchBend: Float,
        val amp: Float,
        val waveShape: WaveShape,
        val updateHarmonicSeries: (HarmonicSeries) -> Unit
    ): PitchTest() 
    { val expectedPitch = calcFreq(this.note, (this.pitchBend * 100).toInt()) }
}

data class PitchTestResults(
    val test: PitchTest.SignalPitchTest,
    val actualPitch: Float
){ val error = calcError(test.expectedPitch, actualPitch) }

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
): Queue<PitchTest> {
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

    val tests = LinkedList<PitchTest>()

    for (note in notes){
        for (pitchBend in pitchBends){
            for (amp in amps){
                for (waveShape in waveShapes){
                    for (hsUpdate in harmonicSeriesUpdates){
                        tests.add(
                            PitchTest.SignalPitchTest(
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