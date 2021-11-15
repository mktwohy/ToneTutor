package com.example.tonetuner_v2

data class Harmonic(var freq: Double, var mag: Double)

data class Spectrum(val freqs: List<Double>, val mags: List<Double>)
