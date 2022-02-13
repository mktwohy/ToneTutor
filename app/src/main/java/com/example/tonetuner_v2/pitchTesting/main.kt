package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.HarmonicFilter.*
import com.example.signallib.enums.Interval
import com.example.signallib.enums.Note
import com.example.signallib.enums.WaveShape.*
import com.example.tonetuner_v2.*
import com.example.tonetuner_v2.audio.audioProcessing.AudioSample
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.example.tonetuner_v2.util.percentage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.lang.StringBuilder
import kotlin.math.roundToInt

fun printThreads(){
    val threads: Set<Thread> = Thread.getAllStackTraces().keys
    println("size: ${threads.size}")
    for (t in threads) {
        val name = t.name
        val state = t.state
        val priority = t.priority
        val type = if (t.isDaemon) "Daemon" else "Normal"
        System.out.printf("%-20s \t %s \t %d \t %s\n", name, state, priority, type)
    }
}

fun main() {
    println(Thread.activeCount())

    val localPath = System.getProperty("user.dir")!! +
            "/app/src/main/java/com/example/tonetuner_v2/pitchTesting/"

    println("Creating pitch tests...")
//    val pitchTests = PitchTest.createTestInputPermutations(
//        numSamples = AppModel.PROC_BUFFER_SIZE,
//        notes = AppModel.NOTE_RANGE,
//        cents = listOf(-25, 0, 25),
//        amps = listOf(1f, 0.5f, 0.25f),
//        waveShapes = listOf(SINE, SAWTOOTH, SQUARE, TRIANGLE),
//        decayRates = listOf(1f, 0.75f, 0.5f, 0.25f),
//        floors = listOf(0f, 0.25f),
//        ceilings = listOf(1f, 0.75f),
//        filters = listOf(ALL, ODD, EVEN)
//    ).shuffled().take(50)

    val pitchTests = SynthPitchTest.createRandomTestInputs(
        numTests = 50,
        numSamples = 1024..4096,
        notes = Note.notes,
        cents = -50..50,
        amps = 0.25f..1f,
        waveShapes = listOf(SINE),
        decayRates = 0f..1f,
        floors = 0f..0.4f,
        ceilings = 0.5f..1f,
        filters = listOf(ALL, ODD, EVEN)
    )

    printThreads()

    print("Running tests...\n\tprogress: ")
    val output = pitchTests.runTests()

    println("\nWriting to file...")
    output.toJson().writeToFile(localPath, "output.json")

    println("Done!")

    printThreads()

//    output.printSummary()

}

fun Any.toJson(): String =
    jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)


fun String.writeToFile(dir: String, name: String) {
    with(File("$dir/$name")){
        writeText(this@writeToFile)
        createNewFile()
    }
}

fun SignalSource.applyTest(test: SynthPitchTest.Input) {
    notes = setOf(test.note)
    pitchBend = test.cents / 100f
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

fun SignalSource.runTest(test: SynthPitchTest.Input): SynthPitchTest.Output{
    this.applyTest(test)
    val audio = this.getAudio(test.numSamples).toMutableList()
    val sample = AudioSample(audioData = audio, pitchAlgo = PitchAlgorithms.twm)
    return SynthPitchTest.Output(sample.pitch)
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

fun Collection<SynthPitchTest.Input>.runTests(): List<SynthPitchTest.CsvCase> {
    val signalSource = SignalSource(40)

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

fun Collection<SynthPitchTest.CsvCase>.printSummary(){
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

