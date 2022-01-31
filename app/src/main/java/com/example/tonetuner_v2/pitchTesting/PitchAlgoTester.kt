package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File


// to clean up file: https://jsonformatter.org/json-pretty-print
fun main() {
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

    val results = pitchTests.runTests()

    writeToFile("pitchTestResults.json", results.toJson())
}

fun Any.toJson(): String
        = jacksonObjectMapper().writeValueAsString(this)

fun writeToFile(name: String, text: String) {
    val dir = System.getProperty("user.dir") ?: return
    val path = "$dir/app/src/main/java/com/example/tonetuner_v2/pitchTesting/$name"
    with(File(path)){
        writeText(text)
        createNewFile()
    }
}

fun SignalSource.applyTest(test: PitchTest.SignalPitchTest) {
    notes = setOf(test.note)
    pitchBend = test.pitchBend
    amp = test.amp
    signalSettings.waveShape = test.waveShape
    test.updateHarmonicSeries(signalSettings.harmonicSeries)
}

fun SignalSource.runTest(test: PitchTest.SignalPitchTest): PitchTestResults{
    this.applyTest(test)
    val audio = this.getAudio(test.numSamples).toMutableList()
    val sample = AudioSample(audioData = audio, pitchAlgo = PitchAlgorithms.twm)
    return PitchTestResults(test, sample.pitch)
}


fun Collection<PitchTest>.runTests(): List<PitchTestResults> {
    val signalSource = SignalSource(AppModel.FINGERPRINT_SIZE)

    return this.map { test ->
        when (test){
            is PitchTest.SignalPitchTest ->
                signalSource.runTest(test)
        }
    }
}
