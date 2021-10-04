package com.example.tonetuner_v2

import com.example.tonetuner_v2.Constants.SAMPLE_RATE
import com.example.tonetuner_v2.Util.arange
import com.example.tonetuner_v2.Util.quadInterp
import com.example.tonetuner_v2.Util.poly
import org.jtransforms.fft.DoubleFFT_1D
import java.lang.Math.log
import kotlin.math.pow
import kotlin.math.roundToInt

data class Harmonic(var freq: Double, var mag: Double)

/**
 * An Audio Sample class to hold and process audio data
 *
 * @constructor other tasty stuff
 * @property fft A List<Double> that holds the Fourier transform of the audio signal
 * @property harmonics
 */
class AudioBuffer(
    audioData: MutableList<Double>
): MutableList<Double> by audioData {
    constructor(): this(emptyList<Double>().toMutableList())

    val time        by lazy { calcTime() }
    val fft         by lazy { calcFFT() }
    val harmonics   by lazy { calcHarmonics() }
    val freq        by lazy { calcFreq() }
    val pitch       by lazy { calcPitch() }
    val fingerprint by lazy { calcFingerprint() }
    val benya       by lazy { calcBenya() }
    val nonNormalizedFingerprint by lazy { calcNonNormalizedFingerprint() }

    companion object{

    }
    fun dropAndAdd(audioData: List<Double>): AudioBuffer {
        val d = this.drop(audioData.size).toMutableList()
        d.addAll(audioData)
        return AudioBuffer(d)
    }

    /**
     * Extract a subsample from the audio sample based on the time.
     *
     * @param start The start time measured from the beginning of the sample in seconds
     * @param size The number of samples in the subsample
     */
    fun subSample(start: Double, size: Int): AudioBuffer {
        // Calculate the nearest starting index.
        val startIndex = (start * SAMPLE_RATE).roundToInt()
        val endIndex = startIndex+size

        return this[startIndex..endIndex]
    }

    /**
     * Extract a subsample from the audio sample based on the time.
     *
     * @param start The start time measured from the beginning of the sample in seconds
     * @param duration The total elapsed time of the subsample
     */
    fun subSample(start: Double, duration: Double): AudioBuffer {
        val start_index = Math.round(start*SAMPLE_RATE).toInt()
        val end_index = (start_index + duration*SAMPLE_RATE).toInt()

        return this[start_index..end_index]
    }

    private fun calcTime(): List<Double> {
        val s = SAMPLE_RATE.toDouble()
        return arange(1/s,this.size/s,1/s)
    }
    /** The fourier transform of the audio data */
    private fun calcFFT(): List<Double> {
        val fd = this.toDoubleArray()
        DoubleFFT_1D(fd.size.toLong()).realForward(fd)
        return fd.toList().asSequence().chunked(2).map {
            Math.sqrt(it[0].pow(2) + it[1].pow(2))
        }.toList()
    }

    /** The frequencies associated with fft */
    private fun calcFreq(): List<Double> {
        return arange(fft.size.toDouble()).map {it*SAMPLE_RATE/(fft.size*2)}
    }

    /** The harmonics extracted from the fourier transform */
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

    /** The pitch (fundamental frequency) of the audio sample */
    private fun calcPitch(): Double {
        val calcScores = twmScore(harmonics)
        val pass1 = arange(-29.0, 7.0).map { n -> 440 * 2.0.pow(n / 12) }
            .map { Harmonic(it, calcScores(it)) }
            .minByOrNull { it.mag }?.freq as Double
        val n = 12 * log(pass1 / 440.0) / log(2.0)
        return arange(n - 1, n + 1, 0.1).map { n -> 440 * 2.0.pow(n / 12) }
            .map { Harmonic(it, calcScores(it)) }
            .minByOrNull { it.mag }?.freq as Double
    }

    /** Calculate the harmonic fingerprint normalized to the total power */
    private fun calcFingerprint(): List<Harmonic> {
        val norm = nonNormalizedFingerprint.asSequence().map { it.mag }.sum()
        return nonNormalizedFingerprint.map { Harmonic(it.freq,it.mag/norm) }
    }

    /** Calculate the harmonic fingerprint */
    private fun calcNonNormalizedFingerprint(): List<Harmonic> {
        val p = pitch
        return arange(1.0,15.0)
            .map { h ->
                val i = Math.round(h*p*fft.size*2/SAMPLE_RATE).toInt()
                Harmonic(h, quadInterp(h*p, freq.slice(i-1..i+1), fft.slice(i-1..i+1)))
            }
    }

    private fun calcBenya(): Double {
        return fingerprint.asSequence().map { it.freq*it.mag }.sum()
    }

    operator fun get(index: IntRange): AudioBuffer {
        return AudioBuffer(index.map { this[it] }.toMutableList())
    }
}

