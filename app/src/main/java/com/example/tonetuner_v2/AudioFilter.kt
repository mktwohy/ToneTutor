package com.example.tonetuner_v2

import java.util.*
import java.util.concurrent.BlockingQueue

/**
 * Thread responsible for processing incoming audio data
 *
 * @param[audioProc]    A reference to an AudioProc thread that is capturing raw
 *                      audio data from the mic
 * @param[plot]         A reference to a Plot object for plotting the FFT etc.
 * @param[signalQueue]  A reference to a queue to put the processed audio data
 */
class AudioFilter(val audioProc: AudioProc,
                  val plot: Plot,
                  //val meterActivity: MeterActivity,
                  val signalQueue: BlockingQueue<Double>,
                  val mainActivity: MainActivity
) : Runnable {

    var running = false

    init {
        running = true
        Thread(this).start()
    }

    override fun run() {
        val frame = 2048                        // Number of samples in one frame of audio data
        var audioSamp = AudioSamp()             // Audio signal processor
        val qualityQueue: LinkedList<Double> = LinkedList()
        val pitchQueue: LinkedList<Double> = LinkedList()
        val numAvg = 10
        val threshold = .01

        while (running) {

            // Fetch 2048 elements from the audioproc
            var d = audioProc.getAudioData(frame)

            // Feed them to the audiosamp
            audioSamp = audioSamp.dropAndAdd(d)

            // Get and plot the fft
            val fft = audioSamp.fft
            plot.update(fft.toTypedArray(), length = fft.size / 8)

            // Calculate the tone score
//            val p = audioSamp.pitch
//            System.out.println(String.format("Fund: %03.2f Benyas: %.2f",
//                    audioSamp.pitch, audioSamp.benya))


//            val benya = audioSamp.benya
//            val pitch = audioSamp.pitch

            if (audioSamp.maxOrNull() ?: 0.0 < threshold) {
                qualityQueue.add(3.0)
                pitchQueue.add(0.0)
            } else {
                qualityQueue.add(audioSamp.benya)
                pitchQueue.add(audioSamp.pitch)
            }


            if (qualityQueue.size > numAvg) {
                qualityQueue.remove()
                pitchQueue.remove()
            }


            mainActivity.updateQuality(qualityQueue.average())
            mainActivity.updatePitch(pitchQueue.average())
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