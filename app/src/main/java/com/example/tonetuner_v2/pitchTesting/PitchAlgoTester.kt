package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File


fun main() {
    println("creating pitch tests...")
    val pitchTests = PitchTest.createPitchTests(
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

    println("running tests...")
    val results = pitchTests.take(20).runTests()

    println("writing to file...")
    writeToFile("pitchTestResults.json", results.toJson())

    println("done!")
}

fun Any.toJson(): String =
    jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)


fun writeToFile(name: String, text: String) {
    val dir = System.getProperty("user.dir") ?: return
    val path = "$dir/app/src/main/java/com/example/tonetuner_v2/pitchTesting/$name"
    with(File(path)){
        writeText(text)
        createNewFile()
    }
}

fun SignalSource.applyTest(test: PitchTest.Input.SignalInput) {
    notes = setOf(test.note)
    pitchBend = test.pitchBend
    amp = test.amp
    signalSettings.waveShape = test.waveShape
    test.updateHarmonicSeries(signalSettings.harmonicSeries)
}

fun SignalSource.runTest(test: PitchTest.Input.SignalInput): PitchTest.Results{
    this.applyTest(test)
    val audio = this.getAudio(test.numSamples).toMutableList()
    val sample = AudioSample(audioData = audio, pitchAlgo = PitchAlgorithms.twm)
    return PitchTest.Results(test, sample.pitch)
}


fun Collection<PitchTest.Input>.runTests(): List<PitchTest.Results> {
    val signalSource = SignalSource(AppModel.FINGERPRINT_SIZE)

    return this.map { test ->
        when (test){
            is PitchTest.Input.SignalInput ->
                signalSource.runTest(test)
        }
    }
}
