package com.example.tonetuner_v2

interface AudioSource {
    fun getAudio(bufferSize: Int): List<Double>
}