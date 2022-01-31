package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.plus
import com.example.signallib.enums.Note.Companion.minus

import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.example.tonetuner_v2.toNoteAndCents
import org.json.JSONObject
import org.json.JSONStringer
import kotlin.math.absoluteValue
import kotlin.math.exp




fun main() {
    val signalSource = SignalSource(AppModel.FINGERPRINT_SIZE)

    val pitchTests = createPitchTests(
        numSamples = AppModel.PROC_BUFFER_SIZE,
        notes = AppModel.NOTE_RANGE,
        pitchBends = listOf(-0.25f, 0f, 0.25f),
        amps = listOf(1f),
        waveShapes = WaveShape.values().toList(),
        decayRates = listOf(0f),
        floors = listOf(0f),
        ceilings = listOf(1f),
        filters = HarmonicFilter.values().toList()
    )

    fun SignalSource.applyTest(test: PitchTest.SignalPitchTest) {
        notes = setOf(test.note)
        pitchBend = test.pitchBend
        amp = test.amp
        signalSettings.waveShape = test.waveShape
        test.updateHarmonicSeries(signalSettings.harmonicSeries)
    }


    Thread {
        for (test in pitchTests.take(20)){
            when (test){
                is PitchTest.SignalPitchTest -> {
                    signalSource.applyTest(test)
                    val audio = signalSource.getAudio(test.numSamples).toMutableList()
                    val sample = AudioSample(audioData = audio, pitchAlgo = PitchAlgorithms.twm)
                    val expectedPitch = calcFreq(test.note, (test.pitchBend * 100).toInt())
                    val actualPitch = sample.pitch
                    val error = calcError(expectedPitch, actualPitch)
                    println("expected: $expectedPitch actual: $actualPitch, error: $error")
                }
            }
        }
    }.start()





}