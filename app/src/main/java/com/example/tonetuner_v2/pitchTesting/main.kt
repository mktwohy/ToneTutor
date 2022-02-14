package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter.*
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.toList
import com.example.signallib.enums.WaveShape.*
import com.example.tonetuner_v2.*
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.example.tonetuner_v2.util.arange
import com.example.tonetuner_v2.util.toList
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import kotlin.math.roundToInt

fun main() {
    val localPath = System.getProperty("user.dir")!! +
            "/app/src/main/java/com/example/tonetuner_v2/pitchTesting/"

    println("Creating pitch tests...")

    val pitchTests = SynthPitchTest.createRandomTestInputs(
        numTests = 10,
//        frequencies = (Note.E_2.freq..Note.E_4.freq).toList(1f),
        frequencies = listOf(Note.A_3.freq),
        bufferSizes = listOf(4096),
//        bufferSizes = listOf(1024, 2048, 4096),
        fingerPrints = Fingerprint.values().toList()
    )

    print("Running tests...\n\tprogress: ")
    val output = pitchTests.runTests()

    println("\nWriting to file...")
    output.toJson().writeToFile(localPath, "output.json")

    println("Done!")
}

fun Any.toJson(): String =
    jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)


fun String.writeToFile(dir: String, name: String) {
    with(File("$dir/$name")){
        writeText(this@writeToFile)
        createNewFile()
    }
}

fun Collection<SynthPitchTest.Input>.runTests(): List<SynthPitchTest.CsvCase> {
    val numHarmonics = Fingerprint.values().maxOf { it.harmonicSeries.numHarmonics }
    val signalSource = SignalSource(numHarmonics)

    return this.mapIndexed { index, testInput ->
        printAsciiProgressBar(
            size = 20,
            percent = ((index + 1) / this.size.toFloat()) * 100,
            clear = index != 0
        )

        val testOutput = signalSource.runTest(testInput)

        SynthPitchTest.CsvCase(testInput, testOutput)
    }
}

fun SignalSource.runTest(test: SynthPitchTest.Input): SynthPitchTest.Output{
    this.applyTest(test)
    val sampleRate = AppModel.SAMPLE_RATE
    val audio = this.getAudio(test.bufferSize).toMutableList()
    val sample = AudioSample(
        audioData = audio,
        pitchAlgo = PitchAlgorithms.twm,
        sampleRate = sampleRate
    )
    return SynthPitchTest.Output(sample.pitch, audio, sampleRate)
}

fun SignalSource.applyTest(test: SynthPitchTest.Input) {
    notes = if (test.note != null) setOf(test.note) else setOf()
    pitchBend = (test.cents ?: 0 ) / 100f
    signalSettings.harmonicSeries = test.harmonicFingerprint.harmonicSeries
}

fun printAsciiProgressBar(size: Int, percent: Float, clear: Boolean){
    fun StringBuilder.repeatAppend(c: Char, times: Int){
        repeat(times){ append(c) }
    }

    with(StringBuilder()){
        if (clear) repeatAppend('\b', times = size)

        val numFilled = (size * (percent / 100)).roundToInt()
        repeatAppend('#', times = numFilled)
        repeatAppend('_', times = size - numFilled)

        print(this.toString())
    }
}