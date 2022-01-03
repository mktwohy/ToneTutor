package com.example.tonetuner_v2.audio.audioSources

import com.example.signallib.*
import com.example.signallib.enums.Note
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.app.AppModel
import kotlin.random.Random

class SignalSource(numHarmonics: Int): AudioSource {
    var notes = setOf(Note.A_4)
    var pitchBend = 0f
    var amp = 1f

    val signalSettings = SignalSettings(
        harmonicSeries = HarmonicSeries(numHarmonics),
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

    fun generateRandom(){
        this.notes = setOf(AppModel.NOTE_RANGE.random())
        this.pitchBend = Random.nextDouble(-0.5, 0.5).toFloat()
        this.signalSettings.harmonicSeries.generateRandom()
    }
}