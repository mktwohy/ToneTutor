package com.example.tonetuner_v2

import com.example.tonetuner_v2.AppModel.FFT_QUEUE_SIZE
import com.example.tonetuner_v2.AppModel.PITCH_QUEUE_SIZE
import com.example.tonetuner_v2.AppModel.PROC_BUFFER_SIZE
import com.example.tonetuner_v2.AppModel.QUALITY_QUEUE_SIZE
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Threaded wrapper class for AudioSample. It continually pulls data from AudioCapture and fills
 * AudioSample. This ensures that attributes [fft], [pitch], and [quality] are always up-to-date
 *
 * @param[audioCapture] [AudioCapture] audio capture for getting mic input
 * @author gtruch and Michael Twohy
 */
class AudioProc(
    val audioCapture: AudioCapture,
    val bufferSize: Int = PROC_BUFFER_SIZE,
) : Runnable {

    private val signalQueue: BlockingQueue<Double> = ArrayBlockingQueue(bufferSize)
    private val fftQueue:       BlockingQueue<List<Double>> = ArrayBlockingQueue(FFT_QUEUE_SIZE)
    private val pitchQueue:     BlockingQueue<Double> = ArrayBlockingQueue(PITCH_QUEUE_SIZE)
    private val qualityQueue:   BlockingQueue<Double> = ArrayBlockingQueue(QUALITY_QUEUE_SIZE)
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
        // todo should I use bufferSize rather than frame?
        var audioSample = AudioSample()            // Audio signal processor
//        val qualityQueue: LinkedList<Double> = LinkedList()
//        val pitchQueue: LinkedList<Double> = LinkedList()
        val threshold = .01
        val pitchDefault = 0.123456
        val qualityDefault = 3.0
        val fftDefault = List(512){ 0.0 } // TEMPORARY SIZE

        while (running) {
            // Fetch [frame] elements from the audioCapture
            val audioData = audioCapture.getAudioData(bufferSize)

            // Feed them to the audioSample
            audioSample = audioSample.dropAndAdd(audioData)

//            val fft = audioSample.fft
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
                fftQueue.forcedOffer(fftDefault)
            } else {
//                qualityQueue.add(audioSample.benya)
//                pitchQueue.add(audioSample.pitch)
                qualityQueue.forcedOffer(audioSample.benya)
                pitchQueue.forcedOffer(audioSample.pitch)
                fftQueue.forcedOffer(audioSample.fft)
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


