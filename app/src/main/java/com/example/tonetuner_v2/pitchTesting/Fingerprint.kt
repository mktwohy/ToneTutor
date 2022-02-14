package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.HarmonicSeries
import com.example.tonetuner_v2.pitchTesting.SynthPitchTest.getFingerPrint
import com.example.tonetuner_v2.util.harmonicSeriesOf

enum class Fingerprint(val harmonicSeries: HarmonicSeries) {
    TEST1 (HarmonicSeries(25).apply { this.generateRandom() }),
    TEST2 (HarmonicSeries(30).apply { this.generateRandom() }),
    TEST3 (HarmonicSeries(40).apply { this.generateRandom() }),
    TEST4 (harmonicSeriesOf(1f, 0.5f, 0.75f, 0.8f, 0.5f, 0.3f, 0.2f, 0.1f, 0.0f))
}