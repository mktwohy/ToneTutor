package com.example.tonetuner_v2

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong
import com.example.signallib.Note.Companion.minus
import com.example.signallib.Note.Companion.plus


// Should I store this as an enum class?

object PitchAlgorithms{

    /** The pitch (fundamental frequency) of the audio sample */
    val twm: (List<Harmonic>) -> Double = { harmonics ->
        // Generate function for comparing different fundamental frequencies
        val calcScores = twmScore(harmonics)

        // estimate the closest note
        val noteEst = AppModel.NOTE_RANGE
            .map { it to calcScores(it.freq.toDouble()) }
            .minByOrNull { it.second }?.first!!

        // define the range of frequencies that are in that note ( +- 1/2 semitone)
        val minFreq = ((noteEst - 1).freq + noteEst.freq) / 2f
        val maxFreq = ((noteEst + 1).freq + noteEst.freq) / 2f

        // refine closest note's frequency and return
        arange(minFreq.toDouble(), maxFreq.toDouble(), 0.1)
            .map { it to calcScores(it) }
            .minByOrNull { it.second}?.first!!
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
