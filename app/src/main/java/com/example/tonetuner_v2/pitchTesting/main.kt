package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.example.tonetuner_v2.median
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.lang.StringBuilder
import kotlin.math.roundToInt



fun main() {
    val localPath = System.getProperty("user.dir") +
            "/app/src/main/java/com/example/tonetuner_v2/pitchTesting/"

    println("Creating pitch tests...")
    val pitchTests = PitchTest.allInputPermutations(
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

    print("Running tests...\n\tprogress: ")
    val output = pitchTests.take(20).runTests()

    println("\nWriting to file...")
    writeToFile("output.json", output.toJson(), localPath)

    println("Done!")

    output.printSummary()
}

fun Any.toJson(): String =
    jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)


fun writeToFile(name: String, text: String, dir: String) {
    with(File("$dir/$name")){
        writeText(text)
        createNewFile()
    }
}

fun SignalSource.applyTest(test: PitchTest.Input.SignalInput) {
    notes = setOf(test.note)
    pitchBend = test.pitchBend
    amp = test.amp
    signalSettings.waveShape = test.waveShape
    signalSettings.harmonicSeries.apply {
        this.generate(
            decayRate = test.harmonicSeriesSettings.decayRate,
            floor = test.harmonicSeriesSettings.floor,
            ceiling = test.harmonicSeriesSettings.ceiling,
            filter = test.harmonicSeriesSettings.filter.function
        )
    }
}

fun SignalSource.runTest(test: PitchTest.Input.SignalInput): PitchTest.Output{
    this.applyTest(test)
    val audio = this.getAudio(test.numSamples).toMutableList()
    val sample = AudioSample(audioData = audio, pitchAlgo = PitchAlgorithms.twm)
    return PitchTest.Output(test, sample.pitch)
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

fun Collection<PitchTest.Input>.runTests(): List<PitchTest.Output> {
    val signalSource = SignalSource(AppModel.FINGERPRINT_SIZE)

    return this.mapIndexed { index, test ->
        when (test){
            is PitchTest.Input.SignalInput -> signalSource.runTest(test)
        }.also {
            printAsciiProgressBar(
                size = 20,
                percent = ((index + 1) / this.size.toFloat()) * 100,
                clear = index != 0
            )
        }
    }
}

fun Collection<PitchTest.Output>.printSummary(){
    val errors = this.map { it.error }
    val avgError    = errors.average()
    val medianError = errors.median()

    val numOctaveError = errors.filter { it.roundToInt() in 95..105}.size
    val numOctaveErrorPercent = (numOctaveError / this.size.toFloat()) * 100

    println("\nSummary:")
    println("\taverage error: $avgError%")
    println("\tmedian error: $medianError")
    println("\toctave errors: $numOctaveError ($numOctaveErrorPercent%)")
}
