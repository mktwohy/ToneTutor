package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter.*
import com.example.signallib.enums.Interval
import com.example.signallib.enums.WaveShape.*
import com.example.tonetuner_v2.*
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.lang.StringBuilder
import kotlin.math.roundToInt

fun main() {
    val localPath = System.getProperty("user.dir")!! +
            "/app/src/main/java/com/example/tonetuner_v2/pitchTesting/"

    println("Creating pitch tests...")
    val pitchTests = PitchTest.allInputPermutations(
        numSamples = AppModel.PROC_BUFFER_SIZE,
        notes = AppModel.NOTE_RANGE,
        pitchBends = listOf(-0.25f, 0f, 0.25f),
        amps = listOf(1f),
        waveShapes = listOf(SINE, SAWTOOTH, SQUARE, TRIANGLE),
        decayRates = listOf(0f),
        floors = listOf(0f),
        ceilings = listOf(1f),
        filters = listOf(ALL, ODD, EVEN)
    ).take(100)

    print("Running tests...\n\tprogress: ")
    val output = pitchTests.runTests()

    println("\nWriting to file...")
    output.toJson().writeToFile(localPath, "output.json")

    println("Done!")

    output.printSummary()
}

fun Any.toJson(): String =
    jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)


fun String.writeToFile(dir: String, name: String) {
    with(File("$dir/$name")){
        writeText(this@writeToFile)
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
            decayRate = test.harmonicSettings.decayRate,
            floor = test.harmonicSettings.floor,
            ceiling = test.harmonicSettings.ceiling,
            filter = test.harmonicSettings.filter.function
        )
    }
}

fun SignalSource.runTest(test: PitchTest.Input.SignalInput): PitchTest.Output{
    this.applyTest(test)
    val audio = this.getAudio(test.numSamples).toMutableList()
    val sample = AudioSample(audioData = audio, pitchAlgo = PitchAlgorithms.twm)
    return PitchTest.Output(sample.pitch)
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

fun Collection<PitchTest.Input>.runTests(): List<PitchTest.TestSummary> {
    val signalSource = SignalSource(AppModel.FINGERPRINT_SIZE)

    return this.mapIndexed { index, testInput ->
        printAsciiProgressBar(
            size = 20,
            percent = ((index + 1) / this.size.toFloat()) * 100,
            clear = index != 0
        )
        val testOutput = when (testInput){
            is PitchTest.Input.SignalInput -> signalSource.runTest(testInput)
        }
        PitchTest.TestSummary(testInput, testOutput)
    }
}

fun <T, R> List<Pair<T, R>>.toPrettyString(
    indentLevel: Int = 0,
    preLine: String = "",
    postLine: String = ""
): String {
    val sb = StringBuilder()
    for ((first, second) in this){
        repeat(indentLevel){ sb.append("\t") }
        sb.append("$preLine$first: $second$postLine\n")
    }
    return sb.toString()
}

fun Collection<PitchTest.TestSummary>.printSummary(){
    val intervalErrorToPercent =
        this.groupBy { it.intervalError }
            .map { it.key to percentage(it.value.size, this.size) }
            .sortedBy { it.second }
            .reversed()

    val score = intervalErrorToPercent.toMap()[Interval.PER_1] ?: 0f

    val errorDistribution = intervalErrorToPercent.toPrettyString(2, postLine = "%")

    println("\nSummary:")
    println("\tscore: $score%")
    println("\terror distribution:\n$errorDistribution")
}

fun getSummary(input: PitchTest.Input, output: PitchTest.Output){

}

