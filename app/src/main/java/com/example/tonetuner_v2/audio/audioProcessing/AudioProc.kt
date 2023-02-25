package com.example.tonetuner_v2.audio.audioProcessing

import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.app.AppModel.CAPTURE_BUFFER_SIZE
import com.example.tonetuner_v2.app.AppModel.FFT_QUEUE_SIZE
import com.example.tonetuner_v2.app.AppModel.FINGERPRINT_QUEUE_SIZE
import com.example.tonetuner_v2.app.AppModel.FINGERPRINT_SIZE
import com.example.tonetuner_v2.app.AppModel.NOISE_THRESHOLD
import com.example.tonetuner_v2.app.AppModel.PITCH_QUEUE_SIZE
import com.example.tonetuner_v2.app.AppModel.PROC_BUFFER_SIZE
import com.example.tonetuner_v2.app.AppModel.QUALITY_QUEUE_SIZE
import com.example.tonetuner_v2.audio.audioSources.AudioSource
import com.example.tonetuner_v2.ui.navigation.MainLayout
import com.example.tonetuner_v2.util.forcedOffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
/**
 * Threaded wrapper class for AudioSample. It continually pulls data from AudioCapture and fills
 * AudioSample. This ensures that attributes [fft], [pitch], and [quality] are always up-to-date
 *
 * @param[audioSource] audio input
 * @author gtruch and Michael Twohy
 */
class AudioProc(
    val audioSource: AudioSource,
    val bufferSize: Int = PROC_BUFFER_SIZE,
    val pitchAlgo: (List<Harmonic>) -> Float
) {
    private val fftQueue: BlockingQueue<List<Harmonic>> = ArrayBlockingQueue(FFT_QUEUE_SIZE)
    private val fingerPrintQueue: BlockingQueue<List<Harmonic>> = ArrayBlockingQueue(FINGERPRINT_QUEUE_SIZE)
    private val pitchQueue: BlockingQueue<Float> = ArrayBlockingQueue(PITCH_QUEUE_SIZE)
    private val qualityQueue: BlockingQueue<Float> = ArrayBlockingQueue(QUALITY_QUEUE_SIZE)
    private var running = false

    val fft: List<Harmonic>
        get() = fftQueue.toList().averageLists()
    val pitch: Float
        get() = pitchQueue.average().toFloat()
    val quality: Float
        get() = qualityQueue.average().toFloat()
    val fingerPrint: List<Harmonic>
        get() = fingerPrintQueue.toList().averageLists()

    init {
        running = true
        start()
    }

    // todo use double buffer to store fft
    // todo add method to stop thread
    fun start() {
        Thread {
            // todo once audioSample is properly mutable, make it a public property
            var audioSample = AudioSample(pitchAlgo = pitchAlgo)
            val pitchDefault = 0f
            val qualityDefault = 0f
            val fingerPrintDefault = List(FINGERPRINT_SIZE) { Harmonic(0f, 0f) }
            val fftDefault = List(CAPTURE_BUFFER_SIZE) { Harmonic(0f, 0f) }

            while (running) {
                // Fetch [bufferSize] elements from the audioCapture
                val audioData = audioSource.getAudio(bufferSize)

                // Feed them to the audioSample
                audioSample = audioSample.dropAndAdd(audioData)

                // Calculate audioSample attributes and add them to their respective queue
                if ((audioSample.maxOrNull() ?: 0f) < NOISE_THRESHOLD) {
                    updateQueues(
                        pitch = pitchDefault,
                        quality = qualityDefault,
                        fingerPrint = fingerPrintDefault,
                        fft = fftDefault
                    )
                } else {
                    updateQueues(
                        pitch = audioSample.pitch,
                        quality = audioSample.benya,
                        fingerPrint = audioSample.fingerprint,
                        fft = audioSample.fft
                    )
                }
            }
        }.start()
    }

    private fun updateQueues(
        pitch: Float,
        quality: Float,
        fingerPrint: List<Harmonic>,
        fft: List<Harmonic>
    ) {
        pitchQueue.forcedOffer(pitch)
        qualityQueue.forcedOffer(quality)
        when (AppModel.spectrumType) {
            MainLayout.SpectrumType.FINGERPRINT -> fingerPrintQueue.forcedOffer(fingerPrint)
            MainLayout.SpectrumType.FFT -> fftQueue.forcedOffer(fft)
        }
    }
}
