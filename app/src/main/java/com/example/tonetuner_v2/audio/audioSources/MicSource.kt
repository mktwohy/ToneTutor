package com.example.tonetuner_v2.audio.audioSources

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.tonetuner_v2.app.AppModel.CAPTURE_BUFFER_SIZE
import com.example.tonetuner_v2.app.AppModel.SAMPLE_RATE
import com.example.tonetuner_v2.checkMicPermission
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Opens the microphone and buffers audio data
 * @author gtruch
 */
class MicSource(
    val context: Context,
    val sampleRate: Int = SAMPLE_RATE,
    val bufferSize: Int = CAPTURE_BUFFER_SIZE,
) : AudioSource {
    private var running = false
    private val queue: BlockingQueue<Float> = ArrayBlockingQueue(bufferSize)

    init {
        startCapture()
    }

    /**
     * Retrieve audio data from the buffer.
     * @param n The number of elements to take
     * @return An ArrayList of data elements
     */
    override fun getAudio(bufferSize: Int) =
        List<Float>(bufferSize) { queue.take() }


    /** Begin capturing and buffering audio */
    fun startCapture() {
        if (running) return

        if (!checkMicPermission(context)){
            running = false
            return
        }

        Thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)

            val buffer = FloatArray(bufferSize)
            val ar = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize
            )

            ar.startRecording()
            running = true
            while (running) {
                // Fetch data from the microphone
                ar.read(buffer, 0,
                    buffer.size, AudioRecord.READ_BLOCKING)

                // Put data into the blocking queue
                buffer.forEach { queue.offer(it) }
            }
            ar.stop()
            ar.release()
        }.start()
    }

    fun stopCapture(){ running = false }


}
