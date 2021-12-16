package com.example.tonetuner_v2

import com.example.signallib.Note
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

// Should I store this as an enum class?

object PitchAlgorithms{

    /** The pitch (fundamental frequency) of the audio sample */
    val twm: (List<Harmonic>) -> Double = { harmonics ->
        // Generate function for comparing different fundamental frequencies
        val calcScores = twmScore(harmonics)

        // todo what do each of these steps do?
//        val pass1 = arange(-29.0, 7.0)      // generate range from -29.0..7.0
//            .map { 440 * 2.0.pow(it / 12) }         // ????
//            .map { Harmonic(it, calcScores(it)) }       //
//            .minByOrNull { it.mag }?.freq as Double

        // pass 1 tests it against each not
        val pass1 = AppModel.NOTE_RANGE
            .map { Harmonic(it.freq.toDouble(), calcScores(it.freq.toDouble())) }
            .minByOrNull { it.mag }?.freq as Double

        //val n = 12 * Math.log(pass1 / 440.0) / Math.log(2.0)
//        logd("pass1: $pass1 n: $n")

        // return
        arange(pass1 - 1, pass1 + 1, 0.1)
//            .map { 440 * 2.0.pow(it / 12) }
            .map { Harmonic(it, calcScores(it)) }
            .minByOrNull { it.mag }?.freq as Double


    }

    val test: (List<Harmonic>) -> Double = { 440.0 }


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
    private fun twmScore(harmonics: List<Harmonic>,
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
}
