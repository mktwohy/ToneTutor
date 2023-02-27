package com.example.tonetuner_v2.app

import android.app.Application
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.MicSource


class App : Application() {
    val audioSource by lazy {
        MicSource(
            sampleRate = AppSettings.SAMPLE_RATE,
            bufferSize = AppSettings.CAPTURE_BUFFER_SIZE
        )
    }
    val audioProcessor by lazy {
        AudioProc(
            audioSource = audioSource,
            bufferSize = AppSettings.PROC_BUFFER_SIZE,
            pitchAlgo = PitchAlgorithms.twm
        )
    }
}
