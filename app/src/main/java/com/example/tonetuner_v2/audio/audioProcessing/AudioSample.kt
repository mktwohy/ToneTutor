package com.example.tonetuner_v2.audio.audioProcessing

import com.example.tonetuner_v2.*
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.app.AppModel.SAMPLE_RATE
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * An Audio Sample class to hold and process audio data
 *
 * @author gtruch and Michael Twohy
 */
class AudioSample(
    audioData: MutableList<Float> = mutableListOf(),
    val sampleRate: Int = SAMPLE_RATE,
    var pitchAlgo: (List<Harmonic>) -> Float = PitchAlgorithms.twm
): MutableList<Float> by audioData {
    val time        by lazy { calcTime() }
    val fftMag      by lazy { calcFftMag() }
    val fft         by lazy { calcFft() }
    val fftFreq     by lazy { calcFftFreq() }
    val pitch       by lazy { pitchAlgo.invoke(fft) }
    val fingerprint by lazy { calcFingerprint() }
    val benya       by lazy { calcBenya() }
    val nonNormalizedFingerprint by lazy { calcNonNormalizedFingerprint() }

    // todo: I don't think drop and add works correctly. Whenever you drop, you end up deleting the whole list
    // todo: This should not create a new object every time.
    // todo: achieve same laziness with more memory efficiency
        // whenever data is updated, set attrs to null
    /* This is more difficult than I thought. lazy delegates cannot be reset, since the property
     * must be immutable. I was looking into making my own delegate class, but I think this will
     * over complicate things... plus, with the solutions I found on StackOverflow, the authors
     * warned about thread safety. Perhaps the simplest solution is private backing variables.
     */
    fun dropAndAdd(audioData: List<Float>): AudioSample {
        val d = this.drop(audioData.size).toMutableList()
        d.addAll(audioData)
        return AudioSample(d)
    }

    /**
     * Extract a subsample from the audio sample based on the time.
     *
     * @param start The start time measured from the beginning of the sample in seconds
     * @param size The number of samples in the subsample
     */
    fun subSample(start: Float, size: Int): AudioSample {
        // Calculate the nearest starting index.
        val startIndex = (start * sampleRate).roundToInt()
        val endIndex = startIndex+size

        return this[startIndex..endIndex]
    }

    /**
     * Extract a subsample from the audio sample based on the time.
     *
     * @param start The start time measured from the beginning of the sample in seconds
     * @param duration The total elapsed time of the subsample
     */
    fun subSample(start: Float, duration: Float): AudioSample {
        val startIndex = (start * sampleRate).roundToInt()
        val endIndex = (startIndex + duration*sampleRate).toInt()

        return this[startIndex..endIndex]
    }

    private fun calcTime(): List<Float> {
        val s = sampleRate.toFloat()
        return arange(1f/s,this.size/s,1f/s)
    }

    /** The fourier transform of the audio data (list size == PROC_BUFFER_SIZE / 2 )*/
    private fun calcFftMag(): List<Float> {
        val fwdTransform = this.toFloatArray()
        FloatFFT_1D(fwdTransform.size.toLong()).realForward(fwdTransform)
        return fwdTransform
            .asSequence()
            .chunked(2)
            .map { sqrt(it[0].pow(2) + it[1].pow(2)) }
            .toList()
    }

    /** The frequencies associated with fft (list size: 1024) */
    private fun calcFftFreq(): List<Float> {
        return arange(fftMag.size.toFloat()).map { it*sampleRate/(fftMag.size*2) }
    }

    //todo does this work correctly? you're creating a list of Spectrums
    /** The harmonics extracted from the fourier transform */
    private fun calcFft(): List<Harmonic> {
        data class Spectrum(val freqs: List<Float>, val mags: List<Float>)

        val maxMag = fftMag.maxOrNull() ?: 0f
        return fftMag.slice(0 until fftMag.size - 2).asSequence()
            .mapIndexed { index, _ ->
                Spectrum(fftFreq.slice(index..index + 2), fftMag.slice(index..index + 2))
            }.filter {
                it.mags[0] < it.mags[1] && it.mags[2] < it.mags[1]
            }.map {
                poly(it.freqs, it.mags)
            }.filter {
                it.mag / maxMag > 0.04f
            }
            .toList()
    }


    /** Calculate the harmonic fingerprint normalized to the total power */
    private fun calcFingerprint(): List<Harmonic> {
        val norm = nonNormalizedFingerprint.map { it.mag }.sum() // todo shouldn't this be maxOf { it } ?
        return nonNormalizedFingerprint.map { Harmonic(it.freq,it.mag / norm) }
    }

    /** Calculate the harmonic fingerprint */
    private fun calcNonNormalizedFingerprint(): List<Harmonic>
        = (1..AppModel.FINGERPRINT_SIZE).map { h ->
            val i = (h * pitch * fftMag.size * 2 / sampleRate).roundToInt()
            if (i > fftFreq.size - 2)
                Harmonic(0f,0f)
            else
                Harmonic(
                    h.toFloat(),
                    quadInterp(
                        h * pitch,
                        fftFreq.slice(i-1..i+1),
                        fftMag.slice(i-1..i+1)
                    )
                )
        }



    private fun calcBenya(): Float {
        return fingerprint.map { it.freq*it.mag }.sum()
    }

    operator fun get(range: IntRange): AudioSample {
        return AudioSample(range.map { this[it] }.toMutableList())
    }
}

