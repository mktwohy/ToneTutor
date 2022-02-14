package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.HarmonicSeries
import com.example.tonetuner_v2.pitchTesting.SynthPitchTest.getFingerPrint
import com.example.tonetuner_v2.util.harmonicSeriesOf
import com.example.signallib.enums.HarmonicFilter.*

enum class Fingerprint(val harmonicSeries: HarmonicSeries) {
    ROUND (HarmonicSeries(25).apply {
        this.generate(
            decayRate = 0.25f,
            ceiling = 1f,
            floor = 0f,
            filter = EVEN.function
        )
    }),
    HARSH (HarmonicSeries(40).apply {
        this.generate(
            decayRate = 0.1f,
            ceiling = 1f,
            floor = 0.25f,
            filter = ODD.function
        )
    }),
    LARGE (HarmonicSeries(100).apply {
        this.generate(
            decayRate = 0.1f,
            ceiling = 1f,
            floor = 0.25f,
            filter = ALL.function
        )
    }),
    SOFT (HarmonicSeries(2).apply {
        this.generate(
            decayRate = 0.1f,
            ceiling = 1f,
            floor = 0.25f,
            filter = ALL.function
        )
    }),
//    TEST4 (harmonicSeriesOf(1f, 0.5f, 0.75f, 0.8f, 0.5f, 0.3f, 0.2f, 0.1f, 0.0f))
}