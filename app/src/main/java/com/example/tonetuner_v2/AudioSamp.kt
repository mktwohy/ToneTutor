package com.example.tonetuner_v2

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.pow
import kotlin.math.roundToInt

data class Harmonic(var freq: Double, var mag: Double)

/**
 * An Audio Sample class to hold and process audio data
 *
 * why aren't the properties showing up?
 * @param gribble
 * @constructor other tasty stuff
 * @property fft A List<Double> that holds the Fourier transform of the audio signal
 * @property harmonics
 */
class AudioSamp(audioData: MutableList<Double>,
                val sampleRate: Double=44100.0) : MutableList<Double>  by audioData {
    constructor(): this(emptyList<Double>().toMutableList())
/*
    constructor(fname: String): this(emptyList<Double>().toMutableList()) {
        val wf = WavFile.openWavFile(File(fname))
        val buffer = DoubleArray(wf.numFrames.toInt())
        wf.readFrames(buffer,wf.numFrames.toInt())
        this.addAll(buffer.toList())
    }
*/
    val time        by lazy { calcTime() }
    val fft         by lazy { calcFFT() }
    val harmonics   by lazy { calcHarmonics() }
    val freq        by lazy { calcFreq() }
    val pitch       by lazy { calcPitch() }
    val fingerprint by lazy { calcFingerprint() }
    val benya       by lazy { calcBenya() }
    val nonNormalizedFingerprint
            by lazy { calcNonNormalizedFingerprint() }

    fun dropAndAdd(audioData: List<Double>): AudioSamp {
        var d = this.drop(audioData.size).toMutableList()
        d.addAll(audioData)
        var ret = AudioSamp(d)
        return ret
    }

    /**
     * Extract a subsample from the audio sample based on the time.
     *
     * @param start The start time measured from the beginning of the sample in seconds
     * @param num The number of samples in the subsample
     */
    fun subSample(start: Double, num: Int): AudioSamp {

        // Calculate the nearest starting index.
        val start_index = Math.round(start*sampleRate).toInt()
        val end_index = start_index+num

        return this[start_index..end_index]
    }

    /**
     * Extract a subsample from the audio sample based on the time.
     *
     * @param start The start time measured from the beginning of the sample in seconds
     * @param duration The total elapsed time of the subsample
     */
    fun subSample(start: Double, duration: Double): AudioSamp {
        val start_index = Math.round(start*sampleRate).toInt()
        val end_index = (start_index + duration*sampleRate).toInt()

        return this[start_index..end_index]
    }

    private fun calcTime(): List<Double> {
        return arange(1/sampleRate,this.size/sampleRate,1/sampleRate)
    }
    /**
     * The fourier transform of the audio data
     */
    private fun calcFFT(): List<Double> {
        val fd = this.toDoubleArray()
        DoubleFFT_1D(fd.size.toLong()).realForward(fd)
        return fd.toList().asSequence().chunked(2).map {
            Math.sqrt(it[0].pow(2) + it[1].pow(2))
        }.toList()
    }

    /**
     * The frequencies associated with fft
     */
    private fun calcFreq(): List<Double> {
        return arange(fft.size.toDouble()).map {it*sampleRate/(fft.size*2)}
    }

    /**
     * The harmonics extracted from the fourier transform
     */
    private fun calcHarmonics(): List<Harmonic> {
        data class Stuff(val freq: List<Double>, val mag: List<Double>)

        val maxMag = fft.maxOrNull() as Double
        return fft.slice(0 until fft.size - 2).asSequence()
                .mapIndexed { index, _ ->
                    Stuff(freq.slice(index..index + 2), fft.slice(index..index + 2))
                }.filter {
                    it.mag[0] < it.mag[1] && it.mag[2] < it.mag[1]
                }.map {
                    poly(it.freq, it.mag)
                }.filter {
                    it.mag / maxMag > 0.04
                }.toList()
    }

    /**
     * The pitch (fundamental frequency) of the audio sample
     */
    private fun  calcPitch(): Double {
        val calcScores = twm_score(harmonics)
        val pass1 = arange(-29.0, 7.0).map { n -> 440 * 2.0.pow(n / 12) }
                .map { Harmonic(it, calcScores(it)) }
                .minByOrNull { it.mag }?.freq as Double
        val n = 12 * Math.log(pass1 / 440.0) / Math.log(2.0)
        return arange(n - 1, n + 1, 0.1).map { n -> 440 * 2.0.pow(n / 12) }
                .map { Harmonic(it, calcScores(it)) }
                .minByOrNull { it.mag }?.freq as Double
    }

    /**
     * Calculate the harmonic fingerprint normalized to the total power
     */
    private fun calcFingerprint(): List<Harmonic> {
        val norm = nonNormalizedFingerprint.asSequence().map { it.mag }.sum()
        return nonNormalizedFingerprint.map { Harmonic(it.freq,it.mag/norm) }
    }

    /**
     * Calculate the harmonic fingerprint
     */
    private fun calcNonNormalizedFingerprint(): List<Harmonic> {
        val p = pitch
        return arange(1.0,15.0)
                .map { h ->
                    val i = Math.round(h*p*fft.size*2/sampleRate).toInt()
                    Harmonic(h, quadInterp(h*p, freq.slice(i-1..i+1), fft.slice(i-1..i+1)))
                }
    }

    private fun calcBenya(): Double {
        return fingerprint.asSequence().map { it.freq*it.mag }.sum()
    }

    operator fun get(index: IntRange): AudioSamp {
        return AudioSamp(index.map { this[it] }.toMutableList())
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
fun twm_score(harmonics: List<Harmonic>,
              p: Double = 0.2,
              q: Double = 1.4,
              r: Double =1.0,
              mtopOnly: Boolean = false, ptomOnly: Boolean = false): (Double) -> Double {

    // Calculate the predicted harmonics of the fundamental frequency
    val maxFreq = harmonics.maxByOrNull { it.freq }?.freq ?: 0.0
    val maxMag = harmonics.maxByOrNull { it.mag }?.mag ?: 0.0

    // Create the lambda
    return { fund ->

        val numHarmonics = Math.round(maxFreq / fund)

        // Generate the harmonics of the given fundamental
        val predictedHarmonics = arange(start = fund, stop = numHarmonics * fund, step = fund)

        // Error based on the distance between each predicted harmonic and its closest measured harmonic
        val err_ptom = predictedHarmonics.map { ph ->
            val h = harmonics.minByOrNull { Math.abs(ph - it.freq) } ?: Harmonic(0.0, 0.0)
            val df = Math.abs(ph - h.freq)
            df * h.freq.pow(-p) + (h.mag / maxMag) * (q * df * h.freq.pow(-p) - r)
        }.sum()

        // Error based on the distance between each measured harmonic and its closest predicted harmonic
        val err_mtop = harmonics.map { h ->
            val ph = predictedHarmonics.minByOrNull {Math.abs(h.freq - it)} ?: 0.0
            val df = Math.abs(h.freq-ph)
            df * h.freq.pow(-p) + (h.mag / maxMag) *(q * df * h.freq.pow(-p) - r)
        }.sum()
        var ret: Double
        if (ptomOnly) ret = err_ptom
        else if (mtopOnly) ret = err_mtop
        else ret =err_ptom / numHarmonics + (0.33) * err_mtop / harmonics.size
        ret
    }
}

fun arange(start: Double, stop: Double? = null, step: Double = 1.0): List<Double> {
    val lstart: Double
    val lstop: Double

    if (stop == null) {
        lstart = 0.0
        lstop = start-1.0
    }
    else {
        lstart = start
        lstop = stop
    }

    val num = ((lstop-lstart)/step).roundToInt() + 1
    return List(num) { index -> step*index + lstart }
}

fun poly(x: List<Double>, y: List<Double>): Harmonic {

    val coef = polyFit(x,y)
    val a = coef[0]
    val b = coef[1]
    val c = coef[2]

    return Harmonic(-b/(2*a), c-b.pow(2)/(4*a))
}

fun quadInterp(x: Double, xvals: List<Double>, yvals: List<Double>): Double {

    val coef = polyFit(xvals,yvals)
    return coef[0]*x.pow(2)+coef[1]*x+coef[2]
}

fun polyFit(x: List<Double>, y: List<Double> ) : List<Double> {
    val denom = (x[0] - x[1])*(x[0] - x[2])*(x[1] - x[2])
    val a = (x[2] * (y[1] - y[0]) + x[1] * (y[0] - y[2]) + x[0] * (y[2] - y[1])) / denom
    val b = (x[2].pow(2) * (y[0] - y[1]) + x[1].pow(2) * (y[2] - y[0]) + x[0].pow(2) * (y[1] - y[2])) / denom
    val c = (x[1]*x[2]*(x[1]-x[2])*y[0]+x[2] * x[0] * (x[2] - x[0]) * y[1] + x[0] * x[1] * (x[0] - x[1]) * y[2]) / denom

    return listOf(a,b,c)
}
