package com.example.tonetuner_v2

import com.example.tonetuner_v2.AppModel.FFT_QUEUE_SIZE
import com.example.tonetuner_v2.AppModel.NOISE_THRESHOLD
import com.example.tonetuner_v2.AppModel.PITCH_QUEUE_SIZE
import com.example.tonetuner_v2.AppModel.PROC_BUFFER_SIZE
import com.example.tonetuner_v2.AppModel.QUALITY_QUEUE_SIZE
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
/**
 * Threaded wrapper class for AudioSample. It continually pulls data from AudioCapture and fills
 * AudioSample. This ensures that attributes [fft], [pitch], and [quality] are always up-to-date
 *
 * @param[audioSource] [AudioCapture] audio capture for getting mic input
 * @author gtruch and Michael Twohy
 */
class AudioProc(
    val audioSource: AudioSource,
    val bufferSize: Int = PROC_BUFFER_SIZE,
) : Runnable {
    private val fftQueue:       BlockingQueue<List<Double>> = ArrayBlockingQueue(FFT_QUEUE_SIZE)
    private val pitchQueue:     BlockingQueue<Double>       = ArrayBlockingQueue(PITCH_QUEUE_SIZE)
    private val qualityQueue:   BlockingQueue<Double>       = ArrayBlockingQueue(QUALITY_QUEUE_SIZE)
    private var running = false

    val fft: List<Double>
        get() = fftQueue.toList().sumLists().map { it/FFT_QUEUE_SIZE }
    val pitch: Double
        get() = pitchQueue.average()
    val quality: Double
        get() = qualityQueue.average()

    init {
        running = true
        Thread(this).start()
    }

    //todo add method to stop thread

    override fun run() {
        // todo once audioSample is properly mutable, make it a public property
        var audioSample = AudioSample(pitchAlgo = testAlgo)
        val pitchDefault = 0.0
        val qualityDefault = 3.5
        val fftDefault = List(512){ 0.0 }

        while (running) {
            // Fetch [bufferSize] elements from the audioCapture
            val audioData = audioSource.getAudio(bufferSize)

            // Feed them to the audioSample
            audioSample = audioSample.dropAndAdd(audioData)

            // Calculate audioSample attributes and add them to their respective queue
            if (audioSample.maxOrNull() ?: 0.0 < NOISE_THRESHOLD) {
                qualityQueue.forcedOffer(qualityDefault)
                pitchQueue.forcedOffer(pitchDefault)
                fftQueue.forcedOffer(fftDefault)
            } else {
                qualityQueue.forcedOffer(audioSample.benya)
                pitchQueue.forcedOffer(audioSample.pitch)
                fftQueue.forcedOffer(audioSample.fft)
            }
        }
    }
}


