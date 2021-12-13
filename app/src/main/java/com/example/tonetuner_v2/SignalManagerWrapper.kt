package com.example.tonetuner_v2

import com.example.signallib.*

class SignalManagerWrapper: AudioSource {
    var notes = setOf(Note.A_4)
    var pitchBend = 0f
    var amp = 1f

    val signalSettings = SignalSettings(
        harmonicSeries = HarmonicSeries(20),
        waveShape = WaveShape.SINE,
        sampleRate = AppModel.SAMPLE_RATE,
        bufferSize = AppModel.CAPTURE_BUFFER_SIZE
    )
    private val signalManager = SignalManager(signalSettings)

    private var audioBuffer = FloatArray(AppModel.CAPTURE_BUFFER_SIZE)

    override fun getAudio(bufferSize: Int): List<Double> {
        if (bufferSize != audioBuffer.size)
            audioBuffer = FloatArray(bufferSize)

        signalManager.renderToBuffer(
            buffer      = audioBuffer,
            notes       = this.notes,
            pitchBend   = this.pitchBend,
            amp         = this.amp
        )
        return audioBuffer.map { it.toDouble() }

    }
}