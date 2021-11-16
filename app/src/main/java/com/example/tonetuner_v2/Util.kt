package com.example.tonetuner_v2

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.tonetuner_v2.Note.Companion.minus
import com.example.tonetuner_v2.Note.Companion.plus
import java.util.concurrent.BlockingQueue
import kotlin.math.*
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

//from https://psychology.wikia.org/wiki/Pitch_perception
fun freqToPitch(freq: Float) = 69 + 12 * log(2f, freq/440f)



/**
 * like toString(), but it returns a substring a desired length. The end is padded with
 * zeros if needed.
 */
fun Double.toString(length: Int) =
    this.toString().padEnd(length, '0').substring(0, length)


/** Similar to offer(), but, if there is no space, it removes an element to make room */
fun <T> BlockingQueue<T>.forcedOffer(element: T){
    if(remainingCapacity() == 0) poll()
    offer(element)
}

fun <T> BlockingQueue<T>.clearAndOffer(element: T){
    clear()
    offer(element)
}

fun List<List<Double>>.sumLists(): List<Double> =
    when(size){
        0 -> listOf()
        1 -> this[0]
        else -> List(this.minOf { it.size } ){ index ->
            var sum = 0.0
            for(list in this){
                sum += list[index]
            }
            sum
        }
    }

operator fun Color.plus(that: Color) =
    Color(
        this.red/2 + that.red/2,
        this.green/2 + that.green/2,
        this.blue/2 + that.blue/2,
        this.alpha/2 + that.alpha/2
    )

/** Converts frequency to closest note estimate*/
fun Double.toNote(): Note?{
    // check if frequency is out of bounds
    if(this < Note.C_0.freq || this > Note.B_8.freq) return null

    // find the upper estimate for the note
    var upperEst = Note.Cs0
    while(upperEst.freq < this && upperEst != Note.B_8){
        upperEst += 1
    }

    // get the lower estimate for the note
    val lowerEst = upperEst - 1

    val upperErr = abs(upperEst.freq - this)
    val lowerErr = abs(lowerEst.freq - this)

    return if(upperErr < lowerErr) upperEst else lowerEst
}

/** Converts frequency to the closest note and its error (cents) */
fun Double.toNoteAndCents(): Pair<Note?, Int>{
    val note = this.toNote() ?: return Pair(null, 0)
    val hzError = this - note.freq

    val centsError =
        if(hzError > 0){
            val hzToNextNote = (note + 1).freq - note.freq
            (100 * hzError/hzToNextNote).toInt()
        } else{
            if(note == Note.C_0){ 0 }
            else{
                val hzToPrevNote =  note.freq - (note - 1).freq
                (100 * hzError/hzToPrevNote).toInt()
            }

        }
    return Pair(note, centsError)
}

fun logd(message: Any){ Log.d("m_tag",message.toString()) }

fun logTime(title: String = "", block: () -> Unit){
    measureTimeMillis { block() }.also { logd("$title $it ms") }
}

fun avgTimeMillis(repeat: Int, block: () -> Unit): Double {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureTimeMillis{ block() }
            .also{ times += it }
    }
    return times.average()
}

fun avgTimeNano(repeat: Int, block: () -> Any?): Double {
    val times = mutableListOf<Long>()
    repeat(repeat){
        measureNanoTime{ block() }
            .also{ times += it }
    }
    return times.average()
}

fun arange(start: Double, stop: Double? = null, step: Double = 1.0): List<Double> {
    val lStart: Double
    val lStop: Double

    if (stop == null) {
        lStart = 0.0
        lStop = start-1.0
    }
    else {
        lStart = start
        lStop = stop
    }

    val size = ((lStop-lStart)/step).roundToInt() + 1
    return List(size) { index -> step*index + lStart }
}

fun poly(x: List<Double>, y: List<Double>): Harmonic {
    val coef = polyFit(x,y)
    val a = coef[0]
    val b = coef[1]
    val c = coef[2]

    return Harmonic(-b/(2*a), c-b.pow(2)/(4*a))
}

fun quadInterp(x: Double, xVals: List<Double>, yVals: List<Double>): Double {
    val coef = polyFit(xVals,yVals)
    return coef[0]*x.pow(2)+coef[1]*x+coef[2]
}

fun polyFit(x: List<Double>, y: List<Double> ) : List<Double> {
    val denom = (x[0] - x[1])*(x[0] - x[2])*(x[1] - x[2])
    val a = (x[2] * (y[1] - y[0]) + x[1] * (y[0] - y[2]) + x[0] * (y[2] - y[1])) / denom
    val b = (x[2].pow(2) * (y[0] - y[1]) + x[1].pow(2) * (y[2] - y[0]) + x[0].pow(2) * (y[1] - y[2])) / denom
    val c = (x[1]*x[2]*(x[1]-x[2])*y[0]+x[2] * x[0] * (x[2] - x[0]) * y[1] + x[0] * x[1] * (x[0] - x[1]) * y[2]) / denom

    return listOf(a,b,c)
}

fun MutableList<Float>.normalize(
    lowerBound: Float = -1f,
    upperBound: Float = 1f
) {
    //Check that array isn't empty
    if (isEmpty()) return

    val minValue   = this.minByOrNull { it }!!
    val maxValue   = this.maxByOrNull { it }!!
    val valueRange = (maxValue - minValue).toFloat()
    val boundRange = (upperBound - lowerBound).toFloat()

    //Check that array isn't already normalized
    // (I would use in range, but this produces excess memory)
    if ((minValue == 0f && maxValue == 0f)
        || (maxValue <= upperBound && maxValue > upperBound
                && minValue >= lowerBound && minValue < lowerBound)) {
        return
    }

    //Normalize
    for (i in indices) {
        this[i] = ((boundRange * (this[i] - minValue)) / valueRange) + lowerBound
    }
}

/**
 * Generates a function to calculate Two Way Mismatch scores for pitch detection of an audio signal.
 *
 *     Pitch detection algorithm described in:
 *          Maher and Beauchamp (1994). "Fundamental frequency estimation of
 *          musical signals using a two-way mismatch procedure," Journal of the
 *          Acoustical Society of America 95, 2254
 *
 * @param harmonics List of harmonics extracted from the fft
 * @param p Optional.  Adjustable parameter for the score calculation
 *          Default = 0.1
 * @param q Optional.  Adjustable parameter for the score calculation
 *          Default = 1.4
 * @param r Optional.  Adjustable parameter for the score calculation
 *          Default = 1.0
 *
 * @return A function that takes a fundamental frequency as input and returns the score
 */
fun twmScore(harmonics: List<Harmonic>,
             p: Double = 0.2,
             q: Double = 1.4,
             r: Double = 1.0,
             mtopOnly: Boolean = false,
             ptomOnly: Boolean = false
): (Double) -> Double {

    // Calculate the predicted harmonics of the fundamental frequency
    val maxFreq = harmonics.maxByOrNull { it.freq }?.freq ?: 0.0
    val maxMag = harmonics.maxByOrNull { it.mag }?.mag ?: 0.0

    // Create the lambda
    return { fund ->
        val numHarmonics = (maxFreq / fund).roundToLong()

        // Generate the harmonics of the given fundamental
        val predictedHarmonics = arange(start = fund, stop = numHarmonics * fund, step = fund)

        // Error based on the distance between each predicted harmonic and its closest measured harmonic
        val err_ptom = predictedHarmonics.map { ph ->
            val h = harmonics.minByOrNull { abs(ph - it.freq) } ?: Harmonic(0.0, 0.0)
            val df = abs(ph - h.freq)
            df * h.freq.pow(-p) + (h.mag / maxMag) * (q * df * h.freq.pow(-p) - r)
        }.sum()

        // Error based on the distance between each measured harmonic and its closest predicted harmonic
        val err_mtop = harmonics.map { h ->
            val ph = predictedHarmonics.minByOrNull { abs(h.freq - it) } ?: 0.0
            val df = abs(h.freq-ph)
            df * h.freq.pow(-p) + (h.mag / maxMag) *(q * df * h.freq.pow(-p) - r)
        }.sum()

        when{
            ptomOnly -> err_ptom
            mtopOnly -> err_mtop
            else     -> err_ptom / numHarmonics + (0.33) * err_mtop / harmonics.size
        }
    }
}