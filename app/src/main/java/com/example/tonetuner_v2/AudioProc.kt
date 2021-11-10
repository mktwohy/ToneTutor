package com.example.tonetuner_v2

import java.util.*
import java.util.concurrent.BlockingQueue

/**
 * Threaded wrapper class for AudioSample. It continually pulls data from AudioCapture and fills
 * AudioSample. This ensures that attributes [fft], [pitch], and [quality] are always up-to-date
 *
 * @param[audioCapture]    A reference to an AudioProc thread that is capturing raw
 *                      audio data from the mic
 * @param[signalQueue]  A reference to a queue to put the processed audio data
 * @author gtruch
 */
class AudioProc(
    val audioCapture: AudioCapture,
    // Todo: Why is signalQueue an parameter?
    val signalQueue: BlockingQueue<Double>,
) : Runnable {

    var running = false

    // Todo: Add fft, pitch, and quality as attributes. idk how to do that while being thread-safe
    init {
        running = true
        Thread(this).start()
    }

    override fun run() {
        val frame = 2048                        // Number of samples in one frame of audio data
        var audioSample = AudioSample()            // Audio signal processor
        val qualityQueue: LinkedList<Double> = LinkedList()
        val pitchQueue: LinkedList<Double> = LinkedList()
        val numAvg = 10
        val threshold = .01

        while (running) {
            // Fetch 2048 elements from the audioCapture
            val audioData = audioCapture.getAudioData(frame)

            // Feed them to the audioSample
            audioSample = audioSample.dropAndAdd(audioData)

            // Get and plot the fft
            val fft = audioSample.fft
//            plot.update(fft.toTypedArray(), length = fft.size / 8)

            // Calculate the tone score
//            val p = audioSamp.pitch
//            System.out.println(String.format("Fund: %03.2f Benyas: %.2f",
//                    audioSamp.pitch, audioSamp.benya))


//            val benya = audioSamp.benya
//            val pitch = audioSamp.pitch

            if (audioSample.maxOrNull() ?: 0.0 < threshold) {
                qualityQueue.add(3.0)
                pitchQueue.add(0.0)
            } else {
                qualityQueue.add(audioSample.benya)
                pitchQueue.add(audioSample.pitch)
            }


            if (qualityQueue.size > numAvg) {
                qualityQueue.remove()
                pitchQueue.remove()
            }


//            mainActivity.updateQuality(qualityQueue.average())
//            mainActivity.updatePitch(pitchQueue.average())
            signalQueue.offer(qualityQueue.average())

            /*
            if (max_amplitude < threshold)
                signalQueue.offer(3.0);
            else
                signalQueue.offer(accum/avg_q.size());
*/
        }
    }
}