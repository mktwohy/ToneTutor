package com.example.tonetuner_v2.audio.audioSources

interface AudioSource {
    fun getAudio(bufferSize: Int): List<Float>
}
