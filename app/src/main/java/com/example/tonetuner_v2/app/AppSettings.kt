package com.example.tonetuner_v2.app

import com.example.signallib.enums.Note

// TODO use persistent storage
object AppSettings {
    const val PROC_BUFFER_SIZE = 2048 // values < 512 cause crash
    const val CAPTURE_BUFFER_SIZE = 2048
    const val SAMPLE_RATE = 44100
    const val UI_LAG = 15L
    const val FFT_QUEUE_SIZE = 3
    const val FINGERPRINT_QUEUE_SIZE = 7
    const val QUALITY_QUEUE_SIZE = 30
    const val PITCH_QUEUE_SIZE = 3
    const val NOISE_THRESHOLD = 0.03f
    const val FINGERPRINT_SIZE = 25
    const val FFT_MAX_FREQ = 20000
    const val TEST_MODE = false
    val NOTE_RANGE = Note.toList(Note.C_1, Note.E_6) // drop C (bass) high E string (guitar)
}
