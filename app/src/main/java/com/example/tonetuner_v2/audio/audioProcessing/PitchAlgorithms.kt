package com.example.tonetuner_v2.audio.audioProcessing

import com.example.signallib.enums.Note.Companion.bend
import com.example.tonetuner_v2.app.AppSettings
import com.example.tonetuner_v2.extensions.step
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

// Should I store this as an enum class?

object PitchAlgorithms {

    /** The pitch (fundamental frequency) of the audio sample */
    val twm: (List<Harmonic>) -> Float = { harmonics ->
        // Generate function for comparing different fundamental frequencies
        val calcScores = twmScore(harmonics)

        // estimate the closest note
        val noteEst = AppSettings.NOTE_RANGE
            .map { it to calcScores(it.freq) }
            .minByOrNull { it.second }?.first!!

        // define the range of frequencies that are in that note ( +- 1/2 semitone)
        val minFreq = noteEst.bend(-0.50f) // 50 cents  flat
        val maxFreq = noteEst.bend(0.50f) // 50 cents sharp

        // refine closest note's frequency and return
        (minFreq..maxFreq step 0.1f)
            .map { it to calcScores(it) }
            .minByOrNull { it.second }?.first!!
    }

//    val twmOLD: (List<Harmonic>) -> Float? = { harmonics ->
//        // Generate function for comparing different fundamental frequencies
//        val calcScores = twmScore(harmonics)
//
//        // todo what do each of these steps do?
//        val pass1 = (-29f..7f step 1f) // generate range from -29.0..7.0
//            .map { 440 * 2f.pow(it / 12) } // ????
//            .map { Harmonic(it, calcScores(it)) } //
//            .minByOrNull { it.mag }?.freq!!
//
//        val n = 12f * ln(pass1 / 440f) / ln(2f)
//
//        // return
//        ((n - 1f)..(n + 1f) step 0.1f)
//            .map { 440 * 2f.pow(it / 12) }
//            .map { Harmonic(it, calcScores(it)) }
//            .minByOrNull { it.mag }?.freq
//    }

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
    private fun twmScore(
        harmonics: List<Harmonic>,
        p: Float = 0.2f,
        q: Float = 1.4f,
        r: Float = 1f,
        mtopOnly: Boolean = false,
        ptomOnly: Boolean = false
    ): (Float) -> Float {

        // Calculate the predicted harmonics of the fundamental frequency
        val maxFreq = harmonics.maxByOrNull { it.freq }?.freq ?: 0f
        val maxMag = harmonics.maxByOrNull { it.mag }?.mag ?: 0f

        // Create the lambda
        return { fund ->
            val numHarmonics = (maxFreq / fund).roundToLong()

            // Generate the harmonics of the given fundamental
            val predictedHarmonics = (fund..(numHarmonics * fund) step fund)

            // Error based on the distance between each predicted harmonic and its closest measured harmonic
            val err_ptom = predictedHarmonics.map { ph ->
                val h = harmonics.minByOrNull { abs(ph - it.freq) } ?: Harmonic(0f, 0f)
                val df = abs(ph - h.freq)
                df * h.freq.pow(-p) + (h.mag / maxMag) * (q * df * h.freq.pow(-p) - r)
            }.sum()

            // Error based on the distance between each measured harmonic and its closest predicted harmonic
            val err_mtop = harmonics.map { h ->
                val ph = predictedHarmonics.minByOrNull { abs(h.freq - it) } ?: 0f
                val df = abs(h.freq - ph)
                df * h.freq.pow(-p) + (h.mag / maxMag) * (q * df * h.freq.pow(-p) - r)
            }.sum()

            when {
                ptomOnly -> err_ptom
                mtopOnly -> err_mtop
                else -> err_ptom / numHarmonics + (0.33f) * err_mtop / harmonics.size
            }
        }
    }
}
