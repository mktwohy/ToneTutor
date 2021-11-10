package com.example.tonetuner_v2

import com.example.tonetuner_v2.Constants.BUFFER_SIZE
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
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
    val bufferSize: Int = BUFFER_SIZE,
) : Runnable {

    private val signalQueue: BlockingQueue<Double> = ArrayBlockingQueue(bufferSize)
    private val numAvg = 10
    private val fftQueue:       BlockingQueue<Double> = ArrayBlockingQueue(numAvg)
    private val pitchQueue:     BlockingQueue<Double> = ArrayBlockingQueue(numAvg)
    private val qualityQueue:   BlockingQueue<Double> = ArrayBlockingQueue(numAvg)
    private var running = false

    // todo not sure how to do fft, since it's not a single number
//    val fft: List<Double>
//        get() = fftQueue.toList()
    val pitch: Double
        get() = pitchQueue.average()
    val quality: Double
        get() = qualityQueue.average()

    // Todo: Add fft, pitch, and quality as attributes. idk how to do that while being thread-safe
    // Perhaps the solution would be that run() updates a blockingQueue, and whenever an attribute
    // calls get(), it takes the average?
    init {
        running = true
        Thread(this).start()
    }

    override fun run() {
        // todo should I use bufferSize rather than frame?
        val frame = 2048 // Number of samples in one frame of audio data
        var audioSample = AudioSample()            // Audio signal processor
//        val qualityQueue: LinkedList<Double> = LinkedList()
//        val pitchQueue: LinkedList<Double> = LinkedList()
        val threshold = .01
        val pitchDefault = 0.123456
        val qualityDefault = 3.0

        while (running) {
            // Fetch 2048 elements from the audioCapture
            val audioData = audioCapture.getAudioData(frame)

            // Feed them to the audioSample
            audioSample = audioSample.dropAndAdd(audioData)

            val fft = audioSample.fft
//            plot.update(fft.toTypedArray(), length = fft.size / 8)

            // Calculate the tone score
//            val p = audioSamp.pitch
//            System.out.println(String.format("Fund: %03.2f Benyas: %.2f",
//                    audioSamp.pitch, audioSamp.benya))


//            val benya = audioSamp.benya
//            val pitch = audioSamp.pitch

            if (audioSample.maxOrNull() ?: 0.0 < threshold) {
//                qualityQueue.add(qualityDefault)
//                pitchQueue.add(pitchDefault)
                qualityQueue.forcedOffer(qualityDefault)
                pitchQueue.forcedOffer(pitchDefault)

            } else {
//                qualityQueue.add(audioSample.benya)
//                pitchQueue.add(audioSample.pitch)
                qualityQueue.forcedOffer(audioSample.benya)
                pitchQueue.forcedOffer(audioSample.pitch)

            }


//            if (qualityQueue.size > numAvg) {
//                qualityQueue.remove()
//                pitchQueue.remove()
//            }

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

fun <T>BlockingQueue<T>.forcedOffer(element: T){
    if(remainingCapacity() == 0) poll()
    offer(element)
}